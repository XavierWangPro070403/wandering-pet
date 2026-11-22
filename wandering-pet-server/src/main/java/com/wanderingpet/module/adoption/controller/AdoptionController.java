package com.wanderingpet.module.adoption.controller;

import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.dto.ApplicationSubmitDTO;
import com.wanderingpet.module.adoption.service.AdoptionService;
import com.wanderingpet.module.adoption.vo.AdoptionRecordVO;
import com.wanderingpet.module.adoption.vo.ApplicationDetailVO;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;
import com.wanderingpet.module.adoption.vo.FlowLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 领养申请接口（申请人视角）
 */
@Tag(name = "领养申请", description = "提交申请、我的申请、进度详情、撤回、领养记录")
@RestController
@RequestMapping("/api/v1/adoption-applications")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    @Operation(summary = "提交领养申请")
    @PostMapping
    public R<Long> submit(@Valid @RequestBody ApplicationSubmitDTO dto) {
        return R.ok(adoptionService.submit(dto));
    }

    @Operation(summary = "我的申请分页")
    @GetMapping("/my")
    public R<PageResult<ApplicationListVO>> my(ApplicationQueryDTO query) {
        return R.ok(adoptionService.pageMy(query));
    }

    @Operation(summary = "我的领养记录（含回访）")
    @GetMapping("/my/records")
    public R<List<AdoptionRecordVO>> myRecords() {
        return R.ok(adoptionService.myRecords());
    }

    @Operation(summary = "申请详情（含流水）")
    @GetMapping("/{id}")
    public R<ApplicationDetailVO> detail(@PathVariable Long id) {
        return R.ok(adoptionService.detail(id));
    }

    @Operation(summary = "状态流水")
    @GetMapping("/{id}/flow-logs")
    public R<List<FlowLogVO>> flowLogs(@PathVariable Long id) {
        return R.ok(adoptionService.flowLogs(id));
    }

    @Operation(summary = "撤回申请")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        adoptionService.cancel(id);
        return R.ok();
    }

    @Operation(summary = "领养记录详情")
    @GetMapping("/records/{recordId}")
    public R<AdoptionRecordVO> recordDetail(@PathVariable Long recordId) {
        return R.ok(adoptionService.recordDetail(recordId));
    }
}
