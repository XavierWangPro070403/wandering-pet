package com.wanderingpet.module.rescue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.module.rescue.dto.ReportQueryDTO;
import com.wanderingpet.module.rescue.entity.RescueReport;
import com.wanderingpet.module.rescue.vo.AdminRescueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 流浪动物线索 Mapper
 */
@Mapper
public interface RescueReportMapper extends BaseMapper<RescueReport> {

    /** 管理端线索分页（关联上报人昵称、物种名） */
    Page<AdminRescueVO> selectAdminPage(Page<AdminRescueVO> page, @Param("q") ReportQueryDTO query);
}
