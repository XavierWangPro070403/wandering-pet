package com.wanderingpet.module.donation.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.donation.dto.DonateDTO;
import com.wanderingpet.module.donation.service.DonationService;
import com.wanderingpet.module.donation.vo.DonationRecordVO;
import com.wanderingpet.module.donation.vo.DonorRankVO;
import com.wanderingpet.module.donation.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 募捐接口（门户）
 */
@Tag(name = "募捐（门户）", description = "项目列表、详情、捐赠（模拟支付）、明细、捐赠榜")
@RestController
@RequestMapping("/api/v1/donation-projects")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @Operation(summary = "进行中项目列表")
    @GetMapping
    public R<List<ProjectVO>> ongoing() {
        return R.ok(donationService.listOngoing());
    }

    @Operation(summary = "项目详情")
    @GetMapping("/{id}")
    public R<ProjectVO> detail(@PathVariable Long id) {
        return R.ok(donationService.detail(id));
    }

    @Operation(summary = "捐赠（模拟支付）")
    @PostMapping("/{id}/donate")
    @OpLog(module = "donation", action = "捐赠")
    public R<Void> donate(@PathVariable Long id, @Valid @RequestBody DonateDTO dto) {
        donationService.donate(id, dto);
        return R.ok();
    }

    @Operation(summary = "捐赠明细（公开）")
    @GetMapping("/{id}/records")
    public R<List<DonationRecordVO>> records(@PathVariable Long id,
                                             @RequestParam(required = false) Integer limit) {
        return R.ok(donationService.recordsOf(id, limit));
    }

    @Operation(summary = "捐赠榜")
    @GetMapping("/{id}/rank")
    public R<List<DonorRankVO>> rank(@PathVariable Long id,
                                     @RequestParam(required = false) Integer topN) {
        return R.ok(donationService.donorRank(id, topN));
    }
}
