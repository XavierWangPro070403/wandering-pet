package com.wanderingpet.module.rescue.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.rescue.dto.ReportHandleDTO;
import com.wanderingpet.module.rescue.dto.ReportQueryDTO;
import com.wanderingpet.module.rescue.dto.RescueReportDTO;
import com.wanderingpet.module.rescue.vo.AdminRescueVO;

/**
 * 流浪动物线索服务
 */
public interface RescueService {

    /** 上报线索（USER） */
    Long report(RescueReportDTO dto);

    /** 我的上报分页 */
    PageResult<AdminRescueVO> pageMy(ReportQueryDTO query);

    /** 管理端线索分页 */
    PageResult<AdminRescueVO> pageAdmin(ReportQueryDTO query);

    /** 分派（PENDING/ASSIGNED → ASSIGNED，指定救助站账号） */
    void assign(Long id, ReportHandleDTO dto);

    /** 开始处理（→ PROCESSING） */
    void process(Long id, ReportHandleDTO dto);

    /** 标记解决（→ RESOLVED） */
    void resolve(Long id, ReportHandleDTO dto);

    /** 关闭（RESOLVED → CLOSED） */
    void close(Long id);
}
