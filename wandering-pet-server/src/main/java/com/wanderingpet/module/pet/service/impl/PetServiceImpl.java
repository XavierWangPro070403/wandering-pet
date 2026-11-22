package com.wanderingpet.module.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.document.UserBehavior;
import com.wanderingpet.common.enums.PetStatus;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.MinioUtil;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.common.util.SeqUtil;
import com.wanderingpet.module.pet.converter.PetConverter;
import com.wanderingpet.module.pet.dto.ImageItem;
import com.wanderingpet.module.pet.dto.PetQueryDTO;
import com.wanderingpet.module.pet.dto.PetSaveDTO;
import com.wanderingpet.module.pet.dto.PetStatusDTO;
import com.wanderingpet.module.pet.entity.Favorite;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.entity.PetBreed;
import com.wanderingpet.module.pet.entity.PetCategory;
import com.wanderingpet.module.pet.entity.PetImage;
import com.wanderingpet.module.pet.mapper.FavoriteMapper;
import com.wanderingpet.module.pet.mapper.PetBreedMapper;
import com.wanderingpet.module.pet.mapper.PetCategoryMapper;
import com.wanderingpet.module.pet.mapper.PetImageMapper;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.pet.service.PetService;
import com.wanderingpet.module.pet.vo.AdminPetVO;
import com.wanderingpet.module.pet.vo.PetDetailVO;
import com.wanderingpet.module.pet.vo.PetImageVO;
import com.wanderingpet.module.pet.vo.PetListVO;
import com.wanderingpet.module.pet.vo.PetManageListVO;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import com.wanderingpet.module.shelter.vo.ShelterSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 宠物档案服务实现
 *
 * <p>缓存策略：详情读 Redis（cache:pet:detail:{id}，30 分钟），先更库后删缓存；
 * 空结果缓存 60 秒防穿透；favorited 标记不入缓存，读出后按登录态补填。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final PetImageMapper petImageMapper;
    private final PetBreedMapper petBreedMapper;
    private final PetCategoryMapper petCategoryMapper;
    private final FavoriteMapper favoriteMapper;
    private final ShelterMapper shelterMapper;
    private final PetConverter petConverter;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final MinioUtil minioUtil;
    private final SeqUtil seqUtil;

    // ---------------------------------------------------------------- 门户

    @Override
    public PageResult<PetListVO> pagePortal(PetQueryDTO query) {
        if (query.getStatus() == null || query.getStatus().isBlank()) {
            query.setStatus(PetStatus.AVAILABLE.name());
        }
        Page<PetListVO> page = petMapper.selectPortalPage(
                new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)), query);
        PageResult<PetListVO> result = PageResult.of(page);
        fillFavorited(result.getList());
        return result;
    }

    @Override
    public PetDetailVO detail(Long id) {
        // 空值缓存保护：60 秒内重复请求不落库
        if (Boolean.TRUE.equals(redisUtil.hasKey(RedisKeyConstant.CACHE_EMPTY.formatted("pet", id)))) {
            throw BizException.of(ResultCode.PET_NOT_FOUND);
        }
        String cacheKey = RedisKeyConstant.CACHE_PET_DETAIL.formatted(id);
        String cached = redisUtil.get(cacheKey);
        if (cached != null) {
            try {
                PetDetailVO vo = objectMapper.readValue(cached, new TypeReference<PetDetailVO>() { });
                fillFavoritedSingle(vo);
                return vo;
            } catch (Exception e) {
                redisUtil.delete(cacheKey);
            }
        }
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            redisUtil.set(RedisKeyConstant.CACHE_EMPTY.formatted("pet", id), "1", Duration.ofSeconds(60));
            throw BizException.of(ResultCode.PET_NOT_FOUND);
        }
        PetDetailVO vo = buildDetail(pet);
        try {
            redisUtil.set(cacheKey, objectMapper.writeValueAsString(vo), Duration.ofMinutes(30));
        } catch (Exception e) {
            log.warn("宠物详情缓存写入失败: {}", e.getMessage());
        }
        fillFavoritedSingle(vo);
        return vo;
    }

    @Override
    public List<PetListVO> similar(Long petId, Integer limit) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw BizException.of(ResultCode.PET_NOT_FOUND);
        }
        List<Pet> pets = petMapper.selectList(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getStatus, PetStatus.AVAILABLE.name())
                .eq(Pet::getCategoryId, pet.getCategoryId())
                .ne(Pet::getId, petId)
                .orderByDesc(Pet::getFavoriteCount)
                .last("LIMIT " + Math.min(limit == null ? 4 : limit, 10)));
        return pets.stream().map(this::toListVO).toList();
    }

    @Override
    public void recordView(Long petId) {
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, petId)
                .setSql("view_count = view_count + 1"));
        redisUtil.zIncrement(RedisKeyConstant.RANK_PET_VIEW_DAY, String.valueOf(petId), 1, Duration.ofHours(24));
        LoginUser user = UserContext.current();
        mongoTemplate.save(UserBehavior.builder()
                .userId(user == null ? null : user.getUserId())
                .behaviorType("VIEW")
                .targetType("PET")
                .targetId(petId)
                .createTime(LocalDateTime.now())
                .build());
    }

    // ---------------------------------------------------------------- 机构/管理端

    @Override
    public PageResult<PetManageListVO> pageShelter(PetQueryDTO query) {
        LoginUser user = requireShelter();
        Page<PetManageListVO> page = petMapper.selectShelterPage(
                new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)), query, user.getShelterId());
        return PageResult.of(page);
    }

    @Override
    public PageResult<AdminPetVO> pageAdmin(PetQueryDTO query) {
        Page<AdminPetVO> page = petMapper.selectAdminPage(
                new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)), query);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(PetSaveDTO dto) {
        LoginUser user = requireShelter();
        Pet pet = petConverter.toEntity(dto);
        pet.setId(null);
        pet.setPetNo(seqUtil.next(BizConstant.SEQ_PET, "PET"));
        pet.setShelterId(user.getShelterId());
        pet.setViewCount(0);
        pet.setFavoriteCount(0);
        if (PetStatus.AVAILABLE.name().equals(dto.getStatus())) {
            pet.setStatus(PetStatus.AVAILABLE.name());
            pet.setPublishTime(LocalDateTime.now());
        } else {
            pet.setStatus(PetStatus.OFFLINE.name());
        }
        petMapper.insert(pet);
        saveImages(pet.getId(), dto.getImages());
        return pet.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PetSaveDTO dto) {
        LoginUser user = requireShelter();
        Pet pet = requireOwnedPet(id, user);
        petConverter.update(pet, dto);
        pet.setId(id);
        pet.setStatus(pet.getStatus());
        petMapper.updateById(pet);
        petImageMapper.delete(new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        saveImages(id, dto.getImages());
        evictCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        LoginUser user = requireShelter();
        Pet pet = requireOwnedPet(id, user);
        petMapper.deleteById(id);
        List<PetImage> images = petImageMapper.selectList(new LambdaQueryWrapper<PetImage>()
                .eq(PetImage::getPetId, id));
        petImageMapper.delete(new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        evictCache(id);
        // 已上传到 MinIO 的对象异步清理（演示数据为 static 托管，objectName 为空则跳过）
        images.stream()
                .filter(img -> img.getObjectName() != null && img.getObjectName().contains("/"))
                .forEach(img -> minioUtil.deleteAsync(img.getObjectName(), false));
        log.info("宠物档案已删除 petId={} petNo={}，待清理图片 {} 张", id, pet.getPetNo(), images.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(PetStatusDTO dto) {
        LoginUser user = requireShelter();
        String status = dto.getStatus();
        if (!PetStatus.isValid(status) || PetStatus.PENDING.name().equals(status)
                || PetStatus.ADOPTED.name().equals(status)) {
            throw BizException.of(ResultCode.BAD_REQUEST, "该状态不允许手工设置");
        }
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            throw BizException.of(ResultCode.BAD_REQUEST, "请选择宠物");
        }
        dto.getIds().forEach(id -> requireOwnedPet(id, user));
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .in(Pet::getId, dto.getIds())
                .eq(Pet::getShelterId, user.getShelterId())
                .set(Pet::getStatus, status)
                .set(PetStatus.AVAILABLE.name().equals(status), Pet::getPublishTime, LocalDateTime.now()));
        dto.getIds().forEach(this::evictCache);
    }

    // ---------------------------------------------------------------- private

    private LoginUser requireShelter() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isShelter() || user.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return user;
    }

    private Pet requireOwnedPet(Long id, LoginUser user) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw BizException.of(ResultCode.PET_NOT_FOUND);
        }
        if (!Objects.equals(pet.getShelterId(), user.getShelterId())) {
            throw BizException.of(ResultCode.FORBIDDEN, "只能操作本机构的宠物");
        }
        return pet;
    }

    private void saveImages(Long petId, List<ImageItem> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (ImageItem item : images) {
            PetImage image = new PetImage();
            image.setPetId(petId);
            image.setObjectName(item.getObjectName() == null ? "" : item.getObjectName());
            image.setUrl(item.getUrl());
            image.setSort(item.getSort() == null ? 0 : item.getSort());
            image.setType("IMAGE");
            petImageMapper.insert(image);
        }
    }

    private PetDetailVO buildDetail(Pet pet) {
        PetDetailVO vo = new PetDetailVO();
        BeanUtils.copyProperties(pet, vo);
        vo.setPublishTime(pet.getPublishTime() == null ? null : pet.getPublishTime().toString().replace('T', ' '));
        // 物种/品种名
        PetCategory category = petCategoryMapper.selectById(pet.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        if (pet.getBreedId() != null) {
            PetBreed breed = petBreedMapper.selectById(pet.getBreedId());
            vo.setBreedName(breed == null ? null : breed.getName());
        }
        vo.setShelter(shelterSimple(pet.getShelterId()));
        // 相册（含封面，封面 sort=0）
        List<PetImage> images = petImageMapper.selectList(new LambdaQueryWrapper<PetImage>()
                .eq(PetImage::getPetId, pet.getId())
                .orderByAsc(PetImage::getSort));
        List<PetImageVO> imageVos = images.stream()
                .map(img -> new PetImageVO(img.getId(), img.getUrl(), img.getObjectName(), img.getSort(), img.getType()))
                .collect(Collectors.toList());
        imageVos.add(0, new PetImageVO(null, pet.getCoverImage(), "", 0, "IMAGE"));
        vo.setImages(imageVos);
        vo.setFavorited(Boolean.FALSE);
        return vo;
    }

    private ShelterSimpleVO shelterSimple(Long shelterId) {
        Shelter shelter = shelterMapper.selectById(shelterId);
        if (shelter == null) {
            return null;
        }
        ShelterSimpleVO vo = new ShelterSimpleVO();
        BeanUtils.copyProperties(shelter, vo);
        return vo;
    }

    private PetListVO toListVO(Pet pet) {
        PetListVO vo = new PetListVO();
        BeanUtils.copyProperties(pet, vo);
        vo.setPublishTime(pet.getPublishTime() == null ? null : pet.getPublishTime().toString().replace('T', ' '));
        vo.setFavorited(Boolean.FALSE);
        return vo;
    }

    /** 批量补填收藏标记（仅登录用户） */
    private void fillFavorited(List<PetListVO> list) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser() || list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(PetListVO::getId).toList();
        var favIds = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, user.getUserId())
                        .eq(Favorite::getTargetType, BizConstant.TARGET_PET)
                        .in(Favorite::getTargetId, ids))
                .stream().map(Favorite::getTargetId).collect(Collectors.toSet());
        list.forEach(vo -> vo.setFavorited(favIds.contains(vo.getId())));
    }

    private void fillFavoritedSingle(PetDetailVO vo) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser() || vo == null) {
            return;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getUserId())
                .eq(Favorite::getTargetType, BizConstant.TARGET_PET)
                .eq(Favorite::getTargetId, vo.getId()));
        vo.setFavorited(count != null && count > 0);
    }

    private void evictCache(Long petId) {
        redisUtil.delete(RedisKeyConstant.CACHE_PET_DETAIL.formatted(petId));
    }
}
