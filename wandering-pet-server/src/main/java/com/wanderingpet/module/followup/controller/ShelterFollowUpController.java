package com.wanderingpet.module.followup.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.followup.service.FollowUpService;
import com.wanderingpet.module.followup.vo.FollowUpVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 机构端回访接口（SHELTER）
 */
@Tag(name = "机构端-回访管理", description = "回访任务列表、催办、归档")
@RestController
@RequestMapping("/api/v1/shelter/follow-ups")
@RequiredArgsConstructor
public class ShelterFollowUpController {

    private final FollowUpService followUpService;

    @Operation(summary = "回访任务分页")
    @RequirePermission("shelter:followup")
    @GetMapping
    public R<PageResult<FollowUpVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) Integer round) {
        return R.ok(followUpService.pageShelter(pageNum, pageSize, status, round));
    }

    @Operation(summary = "催办")
    @RequirePermission("followup:manage")
    @PostMapping("/{id}/remind")
    @OpLog(module = "followup", action = "回访催办")
    public R<Void> remind(@PathVariable Long id) {
        followUpService.remind(id);
        return R.ok();
    }

    @Operation(summary = "归档")
    @RequirePermission("followup:manage")
    @PostMapping("/{id}/archive")
    @OpLog(module = "followup", action = "回访归档")
    public R<Void> archive(@PathVariable Long id) {
        followUpService.archive(id);
        return R.ok();
    }
}
