package com.wanderingpet.module.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.module.pet.dto.PetQueryDTO;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.vo.PetListVO;
import com.wanderingpet.module.pet.vo.PetManageListVO;
import com.wanderingpet.module.pet.vo.AdminPetVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 宠物 Mapper（复杂关联查询走 XML）
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {

    /** 门户/小程序待领养分页列表：关联物种、品种、救助站 */
    Page<PetListVO> selectPortalPage(Page<PetListVO> page, @Param("q") PetQueryDTO query);

    /** 机构端本机构宠物分页列表（全状态） */
    Page<PetManageListVO> selectShelterPage(Page<PetManageListVO> page, @Param("q") PetQueryDTO query,
                                             @Param("shelterId") Long shelterId);

    /** 管理端宠物全量分页列表 */
    Page<AdminPetVO> selectAdminPage(Page<AdminPetVO> page, @Param("q") PetQueryDTO query);
}
