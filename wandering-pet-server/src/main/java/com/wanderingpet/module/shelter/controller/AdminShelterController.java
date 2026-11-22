package com.wanderingpet.module.shelter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.shelter.dto.ShelterAuditDTO;
import com.wanderingpet.module.shelter.service.ShelterService;
import com.wanderingpet.module.shelter.vo.AdminShelterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端机构审核接口
 */
@Tag(name = "管理端-机构审核", description = "机构入驻审核（仅管理员）")
@RestController
@RequestMapping("/api/v1/admin/shelters")
@RequiredArgsConstructor
public class AdminShelterController {

    private final ShelterService shelterService;

    @Operation(summary = "机构分页列表")
    @RequirePermission("shelter:audit")
    @GetMapping
    public R<PageResult<AdminShelterVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Integer auditStatus,
                                              @RequestParam(required = false) String keyword) {
        return R.ok(shelterService.pageAdmin(pageNum, pageSize, auditStatus, keyword));
    }

    @Operation(summary = "入驻审核（通过/驳回）")
    @RequirePermission("shelter:audit")
    @PostMapping("/{id}/audit")
    @OpLog(module = "shelter", action = "入驻审核")
    public R<Void> audit(@PathVariable Long id, @Valid @RequestBody ShelterAuditDTO dto) {
        shelterService.audit(id, dto);
        return R.ok();
    }
}
