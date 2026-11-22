package com.wanderingpet.module.statistics.service;

import com.wanderingpet.module.statistics.vo.AdminDashboardVO;
import com.wanderingpet.module.statistics.vo.PortalSummaryVO;
import com.wanderingpet.module.statistics.vo.ShelterWorkbenchVO;

/**
 * 统计服务
 */
public interface StatisticsService {

    /** 门户数字统计（首页 4 个真实数字） */
    PortalSummaryVO portalSummary();

    /** 机构工作台统计 */
    ShelterWorkbenchVO shelterWorkbench();

    /** 管理端看板 */
    AdminDashboardVO adminDashboard();
}
