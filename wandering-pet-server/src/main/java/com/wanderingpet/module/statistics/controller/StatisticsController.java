package com.wanderingpet.module.statistics.controller;

import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.statistics.service.StatisticsService;
import com.wanderingpet.module.statistics.vo.AdminDashboardVO;
import com.wanderingpet.module.statistics.vo.PortalSummaryVO;
import com.wanderingpet.module.statistics.vo.ShelterWorkbenchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计接口
 */
@Tag(name = "数据统计", description = "门户数字、机构工作台、管理端看板")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "门户数字统计（游客可访问）")
    @GetMapping("/portal-summary")
    public R<PortalSummaryVO> portalSummary() {
        return R.ok(statisticsService.portalSummary());
    }

    @Operation(summary = "机构工作台统计")
    @GetMapping("/shelter-workbench")
    public R<ShelterWorkbenchVO> shelterWorkbench() {
        return R.ok(statisticsService.shelterWorkbench());
    }

    @Operation(summary = "管理端看板")
    @RequirePermission("statistics:view")
    @GetMapping("/admin-dashboard")
    public R<AdminDashboardVO> adminDashboard() {
        return R.ok(statisticsService.adminDashboard());
    }
}
