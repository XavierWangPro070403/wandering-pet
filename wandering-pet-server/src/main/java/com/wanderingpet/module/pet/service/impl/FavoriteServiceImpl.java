package com.wanderingpet.module.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.document.UserBehavior;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.pet.document.PetDynamic;
import com.wanderingpet.module.pet.dto.FavoriteDTO;
import com.wanderingpet.module.pet.entity.Favorite;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.FavoriteMapper;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.pet.service.FavoriteService;
import com.wanderingpet.module.pet.vo.DynamicVO;
import com.wanderingpet.module.pet.vo.FavoritePetVO;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏 + 宠物动态服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PetMapper petMapper;
    private final ShelterMapper shelterMapper;
    private final MongoTemplate mongoTemplate;

    private LoginUser requireUser() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(FavoriteDTO dto) {
        LoginUser user = requireUser();
        Long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getUserId())
                .eq(Favorite::getTargetType, dto.getTargetType())
                .eq(Favorite::getTargetId, dto.getTargetId()));
        if (exists != null && exists > 0) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(user.getUserId());
        favorite.setTargetType(dto.getTargetType());
        favorite.setTargetId(dto.getTargetId());
        favoriteMapper.insert(favorite);
        if (BizConstant.TARGET_PET.equals(dto.getTargetType())) {
            petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                    .eq(Pet::getId, dto.getTargetId())
                    .setSql("favorite_count = favorite_count + 1"));
        }
        mongoTemplate.save(UserBehavior.builder()
                .userId(user.getUserId()).behaviorType("FAVORITE")
                .targetType(dto.getTargetType()).targetId(dto.getTargetId())
                .createTime(LocalDateTime.now()).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(String targetType, Long targetId) {
        LoginUser user = requireUser();
        int removed = favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getUserId())
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId));
        if (removed > 0 && BizConstant.TARGET_PET.equals(targetType)) {
            petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                    .eq(Pet::getId, targetId)
                    .setSql("favorite_count = favorite_count - 1"));
        }
    }

    @Override
    public PageResult<FavoritePetVO> pageMyPetFavorites(Integer pageNum, Integer pageSize) {
        LoginUser user = requireUser();
        Page<Favorite> page = favoriteMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, user.getUserId())
                        .eq(Favorite::getTargetType, BizConstant.TARGET_PET)
                        .orderByDesc(Favorite::getCreateTime));
        List<FavoritePetVO> list = page.getRecords().stream().map(fav -> {
            FavoritePetVO vo = new FavoritePetVO();
            vo.setTargetId(fav.getTargetId());
            vo.setCreateTime(fav.getCreateTime() == null ? null : fav.getCreateTime().toString().replace('T', ' '));
            Pet pet = petMapper.selectById(fav.getTargetId());
            if (pet != null) {
                vo.setName(pet.getName());
                vo.setCoverImage(pet.getCoverImage());
                vo.setAgeMonth(pet.getAgeMonth());
                vo.setStatus(pet.getStatus());
                Shelter shelter = shelterMapper.selectById(pet.getShelterId());
                if (shelter != null) {
                    vo.setShelterName(shelter.getName());
                    vo.setCity(shelter.getCity());
                }
            }
            return vo;
        }).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public boolean isFavorited(String targetType, Long targetId) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getUserId())
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId));
        return count != null && count > 0;
    }

    // ---------------------------------------------------------------- 动态

    @Override
    public List<DynamicVO> listDynamics(Long petId, Integer limit) {
        Query query = new Query(Criteria.where("petId").is(petId))
                .with(Sort.by(Sort.Direction.DESC, "createTime"))
                .limit(Math.min(limit == null ? 10 : limit, 50));
        List<PetDynamic> dynamics = mongoTemplate.find(query, PetDynamic.class);
        return dynamics.stream().map(d -> {
            DynamicVO vo = new DynamicVO();
            vo.setId(d.getId());
            vo.setPetId(d.getPetId());
            vo.setContent(d.getContent());
            vo.setImages(d.getImages());
            vo.setLikeCount(d.getLikeCount());
            vo.setCreateTime(d.getCreateTime() == null ? null : d.getCreateTime().toString().replace('T', ' '));
            return vo;
        }).toList();
    }

    @Override
    public Long saveDynamic(Long petId, String content, List<String> images) {
        LoginUser user = UserContext.current();
        if (user == null || user.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        PetDynamic dynamic = PetDynamic.builder()
                .petId(petId)
                .shelterId(user.getShelterId())
                .content(content)
                .images(images)
                .likeCount(0)
                .createTime(LocalDateTime.now())
                .build();
        mongoTemplate.save(dynamic);
        return petId;
    }
}
