package com.wanderingpet.module.rescue.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.rescue.dto.ReportHandleDTO;
import com.wanderingpet.module.rescue.dto.ReportQueryDTO;
import com.wanderingpet.module.rescue.dto.RescueReportDTO;
import com.wanderingpet.module.rescue.service.RescueService;
import com.wanderingpet.module.rescue.vo.AdminRescueVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 流浪动物线索接口（用户 + 管理端处理）
 */
@Tag(name = "流浪动物上报", description = "线索上报、我的上报、分派处理")
@RestController
@RequestMapping("/api/v1/rescue-reports")
@RequiredArgsConstructor
public class RescueController {

    private final RescueService rescueService;

    @Operation(summary = "上报线索")
    @PostMapping
    @OpLog(module = "rescue", action = "上报流浪动物线索")
    public R<Long> report(@Valid @RequestBody RescueReportDTO dto) {
        return R.ok(rescueService.report(dto));
    }

    @Operation(summary = "我的上报")
    @GetMapping("/my")
    public R<PageResult<AdminRescueVO>> my(ReportQueryDTO query) {
        return R.ok(rescueService.pageMy(query));
    }
}
