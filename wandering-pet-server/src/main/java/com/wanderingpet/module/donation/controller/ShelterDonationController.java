package com.wanderingpet.module.donation.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.donation.dto.ProjectSaveDTO;
import com.wanderingpet.module.donation.service.DonationService;
import com.wanderingpet.module.donation.vo.DonationRecordVO;
import com.wanderingpet.module.donation.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机构端募捐管理（SHELTER）
 */
@Tag(name = "机构端-募捐管理", description = "项目维护、捐赠明细")
@RestController
@RequestMapping("/api/v1/shelter/donation-projects")
@RequiredArgsConstructor
public class ShelterDonationController {

    private final DonationService donationService;

    @Operation(summary = "本机构项目列表")
    @RequirePermission("shelter:donation")
    @GetMapping
    public R<List<ProjectVO>> my() {
        return R.ok(donationService.myProjects());
    }

    @Operation(summary = "新建募捐项目")
    @RequirePermission("donation:manage")
    @PostMapping
    @OpLog(module = "donation", action = "新建募捐项目")
    public R<Long> save(@Valid @RequestBody ProjectSaveDTO dto) {
        return R.ok(donationService.saveProject(dto));
    }

    @Operation(summary = "更新募捐项目")
    @RequirePermission("donation:manage")
    @PutMapping("/{id}")
    @OpLog(module = "donation", action = "更新募捐项目")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ProjectSaveDTO dto) {
        donationService.updateProject(id, dto);
        return R.ok();
    }

    @Operation(summary = "关闭募捐项目")
    @RequirePermission("donation:manage")
    @PostMapping("/{id}/close")
    @OpLog(module = "donation", action = "关闭募捐项目")
    public R<Void> close(@PathVariable Long id) {
        donationService.closeProject(id);
        return R.ok();
    }

    @Operation(summary = "本机构捐赠明细")
    @RequirePermission("donation:record:view")
    @GetMapping("/records")
    public R<PageResult<DonationRecordVO>> records(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) Long projectId) {
        return R.ok(donationService.myRecords(pageNum, pageSize, projectId));
    }
}
