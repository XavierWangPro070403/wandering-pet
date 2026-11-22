package com.wanderingpet.module.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.pet.entity.PetHealthRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物健康档案 Mapper
 */
@Mapper
public interface PetHealthRecordMapper extends BaseMapper<PetHealthRecord> {
}
