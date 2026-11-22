package com.wanderingpet.module.adoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.entity.AdoptionApplication;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 领养申请 Mapper（分页关联宠物/用户名走 XML）
 */
@Mapper
public interface AdoptionApplicationMapper extends BaseMapper<AdoptionApplication> {

    /** 分页查询申请列表（机构端/管理端，含宠物名、申请人昵称） */
    Page<ApplicationListVO> selectPageWithNames(Page<ApplicationListVO> page, @Param("q") ApplicationQueryDTO query);

    /** 申请人视角分页（含宠物信息） */
    Page<ApplicationListVO> selectMyPage(Page<ApplicationListVO> page, @Param("q") ApplicationQueryDTO query);
}
