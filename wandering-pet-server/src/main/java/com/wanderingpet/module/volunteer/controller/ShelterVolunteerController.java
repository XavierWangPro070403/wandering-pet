package com.wanderingpet.module.volunteer.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.volunteer.dto.VolunteerAuditDTO;
import com.wanderingpet.module.volunteer.service.VolunteerService;
import com.wanderingpet.module.volunteer.vo.VolunteerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 机构端志愿者审核（SHELTER）
 */
@Tag(name = "机构端-志愿者审核", description = "报名审核")
@RestController
@RequestMapping("/api/v1/shelter/volunteer-applications")
@RequiredArgsConstructor
public class ShelterVolunteerController {

    private final VolunteerService volunteerService;

    @Operation(summary = "报名审核分页")
    @RequirePermission("shelter:volunteer")
    @GetMapping
    public R<PageResult<VolunteerVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String status) {
        return R.ok(volunteerService.pageShelter(pageNum, pageSize, status));
    }

    @Operation(summary = "审核")
    @RequirePermission("volunteer:audit")
    @PostMapping("/{id}/audit")
    @OpLog(module = "volunteer", action = "志愿者审核")
    public R<Void> audit(@PathVariable Long id, @Valid @RequestBody VolunteerAuditDTO dto) {
        volunteerService.audit(id, dto);
        return R.ok();
    }
}
