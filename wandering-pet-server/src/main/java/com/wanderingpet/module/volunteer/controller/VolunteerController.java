package com.wanderingpet.module.volunteer.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.volunteer.dto.VolunteerApplyDTO;
import com.wanderingpet.module.volunteer.service.VolunteerService;
import com.wanderingpet.module.volunteer.vo.VolunteerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 志愿者接口（用户）
 */
@Tag(name = "志愿者", description = "报名、我的报名")
@RestController
@RequestMapping("/api/v1/volunteer-applications")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @Operation(summary = "报名志愿者")
    @PostMapping
    @OpLog(module = "volunteer", action = "志愿者报名")
    public R<Long> apply(@Valid @RequestBody VolunteerApplyDTO dto) {
        return R.ok(volunteerService.apply(dto));
    }

    @Operation(summary = "我的报名")
    @GetMapping("/my")
    public R<PageResult<VolunteerVO>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(volunteerService.pageMy(pageNum, pageSize));
    }
}
