package com.wanderingpet.module.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.pet.dto.HealthRecordDTO;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.entity.PetBreed;
import com.wanderingpet.module.pet.entity.PetCategory;
import com.wanderingpet.module.pet.entity.PetHealthRecord;
import com.wanderingpet.module.pet.mapper.PetBreedMapper;
import com.wanderingpet.module.pet.mapper.PetCategoryMapper;
import com.wanderingpet.module.pet.mapper.PetHealthRecordMapper;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.pet.service.CategoryService;
import com.wanderingpet.module.pet.vo.BreedVO;
import com.wanderingpet.module.pet.vo.CategoryVO;
import com.wanderingpet.module.pet.vo.HealthRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 分类 / 品种 / 健康档案服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final PetCategoryMapper petCategoryMapper;
    private final PetBreedMapper petBreedMapper;
    private final PetHealthRecordMapper healthRecordMapper;
    private final PetMapper petMapper;

    @Override
    public List<CategoryVO> listCategories() {
        return petCategoryMapper.selectList(new LambdaQueryWrapper<PetCategory>()
                        .orderByAsc(PetCategory::getSort)).stream()
                .map(c -> new CategoryVO(c.getId(), c.getName(), c.getIcon(), c.getSort()))
                .toList();
    }

    @Override
    public List<BreedVO> listBreeds(Long categoryId) {
        return petBreedMapper.selectList(new LambdaQueryWrapper<PetBreed>()
                        .eq(categoryId != null, PetBreed::getCategoryId, categoryId)
                        .orderByAsc(PetBreed::getSort)).stream()
                .map(b -> new BreedVO(b.getId(), b.getCategoryId(), b.getName()))
                .toList();
    }

    @Override
    public List<HealthRecordVO> healthRecords(Long petId) {
        return healthRecordMapper.selectList(new LambdaQueryWrapper<PetHealthRecord>()
                        .eq(PetHealthRecord::getPetId, petId)
                        .orderByDesc(PetHealthRecord::getRecordDate)).stream()
                .map(this::toVO).toList();
    }

    @Override
    public Long saveHealthRecord(HealthRecordDTO dto) {
        Pet pet = requireOwnedPet(dto.getPetId());
        PetHealthRecord record = new PetHealthRecord();
        BeanUtils.copyProperties(dto, record);
        record.setPetId(pet.getId());
        record.setRecordDate(LocalDate.parse(dto.getRecordDate()));
        healthRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void updateHealthRecord(Long id, HealthRecordDTO dto) {
        PetHealthRecord record = healthRecordMapper.selectById(id);
        if (record == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        requireOwnedPet(record.getPetId());
        if (dto.getRecordType() != null) {
            record.setRecordType(dto.getRecordType());
        }
        if (dto.getRecordDate() != null && !dto.getRecordDate().isBlank()) {
            record.setRecordDate(LocalDate.parse(dto.getRecordDate()));
        }
        if (dto.getTitle() != null) {
            record.setTitle(dto.getTitle());
        }
        record.setContent(dto.getContent() == null ? "" : dto.getContent());
        record.setVetName(dto.getVetName() == null ? "" : dto.getVetName());
        record.setAttachmentUrl(dto.getAttachmentUrl() == null ? "" : dto.getAttachmentUrl());
        healthRecordMapper.updateById(record);
    }

    @Override
    public void deleteHealthRecord(Long id) {
        PetHealthRecord record = healthRecordMapper.selectById(id);
        if (record == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        requireOwnedPet(record.getPetId());
        healthRecordMapper.deleteById(id);
    }

    private Pet requireOwnedPet(Long petId) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isShelter() || user.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw BizException.of(ResultCode.PET_NOT_FOUND);
        }
        if (!Objects.equals(pet.getShelterId(), user.getShelterId())) {
            throw BizException.of(ResultCode.FORBIDDEN, "只能操作本机构的宠物");
        }
        return pet;
    }

    private HealthRecordVO toVO(PetHealthRecord record) {
        HealthRecordVO vo = new HealthRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setRecordDate(record.getRecordDate() == null ? null : record.getRecordDate().toString());
        return vo;
    }
}
