package com.wanderingpet.module.rescue.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.rescue.dto.ReportHandleDTO;
import com.wanderingpet.module.rescue.dto.ReportQueryDTO;
import com.wanderingpet.module.rescue.service.RescueService;
import com.wanderingpet.module.rescue.vo.AdminRescueVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 线索处理接口（管理员 + 救助站）
 */
@Tag(name = "线索处理", description = "分派、处理、解决、关闭")
@RestController
@RequiredArgsConstructor
public class RescueHandleController {

    private final RescueService rescueService;

    @Operation(summary = "管理端线索分页")
    @RequirePermission("rescue:manage")
    @GetMapping("/api/v1/admin/rescue-reports")
    public R<PageResult<AdminRescueVO>> page(ReportQueryDTO query) {
        return R.ok(rescueService.pageAdmin(query));
    }

    @Operation(summary = "分派（管理员）")
    @RequirePermission("rescue:manage")
    @PostMapping("/api/v1/admin/rescue-reports/{id}/assign")
    @OpLog(module = "rescue", action = "线索分派")
    public R<Void> assign(@PathVariable Long id, @RequestBody ReportHandleDTO dto) {
        rescueService.assign(id, dto);
        return R.ok();
    }

    @Operation(summary = "关闭（管理员）")
    @RequirePermission("rescue:manage")
    @PostMapping("/api/v1/admin/rescue-reports/{id}/close")
    @OpLog(module = "rescue", action = "线索关闭")
    public R<Void> close(@PathVariable Long id) {
        rescueService.close(id);
        return R.ok();
    }

    @Operation(summary = "开始处理（救助站）")
    @PostMapping("/api/v1/shelter/rescue-reports/{id}/process")
    @OpLog(module = "rescue", action = "线索处理")
    public R<Void> process(@PathVariable Long id, @RequestBody(required = false) ReportHandleDTO dto) {
        rescueService.process(id, dto == null ? new ReportHandleDTO() : dto);
        return R.ok();
    }

    @Operation(summary = "标记解决（救助站）")
    @PostMapping("/api/v1/shelter/rescue-reports/{id}/resolve")
    @OpLog(module = "rescue", action = "线索解决")
    public R<Void> resolve(@PathVariable Long id, @RequestBody(required = false) ReportHandleDTO dto) {
        rescueService.resolve(id, dto == null ? new ReportHandleDTO() : dto);
        return R.ok();
    }
}
