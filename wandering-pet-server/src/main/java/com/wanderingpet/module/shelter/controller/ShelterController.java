package com.wanderingpet.module.shelter.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.shelter.dto.ShelterApplyDTO;
import com.wanderingpet.module.shelter.dto.ShelterUpdateDTO;
import com.wanderingpet.module.shelter.service.ShelterService;
import com.wanderingpet.module.shelter.vo.ShelterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 救助站接口（门户可浏览；入驻申请/信息维护需登录）
 */
@Tag(name = "救助站", description = "机构列表、详情、入驻申请、机构信息维护")
@RestController
@RequestMapping("/api/v1/shelters")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    @Operation(summary = "已入驻救助站列表")
    @GetMapping
    public R<List<ShelterVO>> list(@Parameter(description = "城市，可空") @RequestParam(required = false) String city) {
        return R.ok(shelterService.listApproved(city));
    }

    @Operation(summary = "救助站详情")
    @GetMapping("/{id}")
    public R<ShelterVO> detail(@PathVariable Long id) {
        return R.ok(shelterService.detail(id));
    }

    @Operation(summary = "申请入驻（C 端用户）")
    @PostMapping("/apply")
    @OpLog(module = "shelter", action = "提交入驻申请")
    public R<Long> apply(@Valid @RequestBody ShelterApplyDTO dto) {
        return R.ok(shelterService.apply(dto));
    }

    @Operation(summary = "我的机构信息（SHELTER）")
    @GetMapping("/me")
    public R<ShelterVO> myShelter() {
        return R.ok(shelterService.myShelter());
    }

    @Operation(summary = "更新我的机构信息（SHELTER）")
    @PutMapping("/me")
    @OpLog(module = "shelter", action = "更新机构信息")
    public R<Void> updateMine(@Valid @RequestBody ShelterUpdateDTO dto) {
        shelterService.updateMine(dto);
        return R.ok();
    }
}
