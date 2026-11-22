package com.wanderingpet.module.donation.controller;

import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.donation.service.DonationService;
import com.wanderingpet.module.donation.vo.DonationRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端捐赠明细（ADMIN）
 */
@Tag(name = "管理端-捐赠明细", description = "全平台捐赠明细查询（仅管理员）")
@RestController
@RequestMapping("/api/v1/admin/donation-records")
@RequiredArgsConstructor
public class AdminDonationController {

    private final DonationService donationService;

    @Operation(summary = "捐赠明细分页")
    @RequirePermission("donation:record:view")
    @GetMapping
    public R<PageResult<DonationRecordVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) Long projectId) {
        return R.ok(donationService.pageAdminRecords(pageNum, pageSize, projectId));
    }
}
