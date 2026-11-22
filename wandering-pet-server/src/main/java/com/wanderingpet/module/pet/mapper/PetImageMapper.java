package com.wanderingpet.module.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.pet.entity.PetImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物相册 Mapper
 */
@Mapper
public interface PetImageMapper extends BaseMapper<PetImage> {
}
