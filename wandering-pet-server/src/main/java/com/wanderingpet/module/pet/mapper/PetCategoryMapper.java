package com.wanderingpet.module.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.pet.entity.PetCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物物种 Mapper
 */
@Mapper
public interface PetCategoryMapper extends BaseMapper<PetCategory> {
}
