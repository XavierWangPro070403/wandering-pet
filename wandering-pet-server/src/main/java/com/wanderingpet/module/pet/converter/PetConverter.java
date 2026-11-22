package com.wanderingpet.module.pet.converter;

import com.wanderingpet.module.pet.dto.PetSaveDTO;
import com.wanderingpet.module.pet.entity.Pet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 宠物档案对象转换（MapStruct；相册与状态在 Service 中单独处理）
 */
@Mapper(componentModel = "spring")
public interface PetConverter {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Pet toEntity(PetSaveDTO dto);

    /** 编辑时把非空字段拷入已加载实体（忽略 petNo/shelterId 等受保护字段） */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@org.mapstruct.MappingTarget Pet pet, PetSaveDTO dto);
}
