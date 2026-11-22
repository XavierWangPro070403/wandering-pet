package com.wanderingpet.module.adoption.controller;

import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.service.AdoptionAuditService;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端领养申请查询
 */
@Tag(name = "管理端-领养管理", description = "全平台领养申请查询（仅管理员）")
@RestController
@RequestMapping("/api/v1/admin/applications")
@RequiredArgsConstructor
public class AdminAdoptionController {

    private final AdoptionAuditService adoptionAuditService;

    @Operation(summary = "领养申请全量分页")
    @RequirePermission("adoption:manage-all")
    @GetMapping
    public R<PageResult<ApplicationListVO>> page(ApplicationQueryDTO query) {
        return R.ok(adoptionAuditService.pageAdmin(query));
    }
}
