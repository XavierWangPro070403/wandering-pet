package com.wanderingpet.module.adoption.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.dto.AuditActionDTO;
import com.wanderingpet.module.adoption.dto.HomeVisitDTO;
import com.wanderingpet.module.adoption.dto.InterviewDTO;
import com.wanderingpet.module.adoption.dto.ReturnDTO;
import com.wanderingpet.module.adoption.dto.SignDTO;
import com.wanderingpet.module.adoption.service.AdoptionAuditService;
import com.wanderingpet.module.adoption.vo.AdoptionRecordVO;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 机构端领养审核接口（SHELTER）
 */
@Tag(name = "机构端-领养审核", description = "受理、面谈、家访、通过、驳回、签约、完成、退养")
@RestController
@RequestMapping("/api/v1/shelter")
@RequiredArgsConstructor
public class ShelterAdoptionController {

    private final AdoptionAuditService adoptionAuditService;

    @Operation(summary = "本机构申请分页")
    @RequirePermission("shelter:adoption")
    @GetMapping("/applications")
    public R<PageResult<ApplicationListVO>> page(ApplicationQueryDTO query) {
        return R.ok(adoptionAuditService.pageShelter(query));
    }

    @Operation(summary = "受理（SUBMITTED→REVIEWING）")
    @RequirePermission("adoption:review")
    @PostMapping("/applications/{id}/accept")
    @OpLog(module = "adoption", action = "受理申请")
    public R<Void> accept(@PathVariable Long id) {
        adoptionAuditService.accept(id);
        return R.ok();
    }

    @Operation(summary = "排期面谈（REVIEWING→INTERVIEW）")
    @RequirePermission("adoption:review")
    @PostMapping("/applications/{id}/interview")
    @OpLog(module = "adoption", action = "面谈排期")
    public R<Void> interview(@PathVariable Long id, @RequestBody InterviewDTO dto) {
        adoptionAuditService.interview(id, dto);
        return R.ok();
    }

    @Operation(summary = "排期家访（INTERVIEW→HOME_VISIT）")
    @RequirePermission("adoption:review")
    @PostMapping("/applications/{id}/home-visit")
    @OpLog(module = "adoption", action = "家访排期")
    public R<Void> homeVisit(@PathVariable Long id, @RequestBody HomeVisitDTO dto) {
        adoptionAuditService.homeVisit(id, dto);
        return R.ok();
    }

    @Operation(summary = "审核通过（HOME_VISIT→APPROVED）")
    @RequirePermission("adoption:review")
    @PostMapping("/applications/{id}/approve")
    @OpLog(module = "adoption", action = "审核通过")
    public R<Void> approve(@PathVariable Long id, @RequestBody(required = false) AuditActionDTO dto) {
        adoptionAuditService.approve(id, dto == null ? new AuditActionDTO() : dto);
        return R.ok();
    }

    @Operation(summary = "驳回（REVIEWING/INTERVIEW/HOME_VISIT→REJECTED）")
    @RequirePermission("adoption:review")
    @PostMapping("/applications/{id}/reject")
    @OpLog(module = "adoption", action = "驳回申请")
    public R<Void> reject(@PathVariable Long id, @RequestBody AuditActionDTO dto) {
        adoptionAuditService.reject(id, dto);
        return R.ok();
    }

    @Operation(summary = "签约（APPROVED→SIGNED）")
    @RequirePermission("adoption:sign")
    @PostMapping("/applications/{id}/sign")
    @OpLog(module = "adoption", action = "签署协议")
    public R<Void> sign(@PathVariable Long id, @RequestBody(required = false) SignDTO dto) {
        adoptionAuditService.sign(id, dto == null ? new SignDTO() : dto);
        return R.ok();
    }

    @Operation(summary = "完成领养（SIGNED→ADOPTED，生成回访计划）")
    @RequirePermission("adoption:complete")
    @PostMapping("/applications/{id}/complete")
    @OpLog(module = "adoption", action = "完成领养")
    public R<Void> complete(@PathVariable Long id) {
        adoptionAuditService.complete(id);
        return R.ok();
    }

    @Operation(summary = "本机构领养记录分页")
    @RequirePermission("adoption:review")
    @GetMapping("/adoption-records")
    public R<PageResult<AdoptionRecordVO>> records(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String status) {
        return R.ok(adoptionAuditService.pageRecords(null, pageNum, pageSize, status));
    }

    @Operation(summary = "退养登记")
    @RequirePermission("adoption:complete")
    @PostMapping("/adoption-records/{id}/return")
    @OpLog(module = "adoption", action = "退养登记")
    public R<Void> returnAdoption(@PathVariable Long id, @RequestBody ReturnDTO dto) {
        adoptionAuditService.returnAdoption(id, dto);
        return R.ok();
    }
}
