package com.wanderingpet.module.pet.service;

import com.wanderingpet.module.pet.vo.PetImageVO;
import com.wanderingpet.module.pet.vo.HealthRecordVO;

import java.util.List;

/**
 * 分类 / 品种 / 健康档案服务
 */
public interface CategoryService {

    /** 物种分类（启用中，按 sort） */
    List<com.wanderingpet.module.pet.vo.CategoryVO> listCategories();

    /** 品种列表（可按物种过滤） */
    List<com.wanderingpet.module.pet.vo.BreedVO> listBreeds(Long categoryId);

    // ---------- 健康档案 ----------

    List<HealthRecordVO> healthRecords(Long petId);

    Long saveHealthRecord(com.wanderingpet.module.pet.dto.HealthRecordDTO dto);

    void updateHealthRecord(Long id, com.wanderingpet.module.pet.dto.HealthRecordDTO dto);

    void deleteHealthRecord(Long id);
}
