package com.wanderingpet.module.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.pet.entity.PetBreed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物品种 Mapper
 */
@Mapper
public interface PetBreedMapper extends BaseMapper<PetBreed> {
}
