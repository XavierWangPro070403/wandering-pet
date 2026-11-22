package com.wanderingpet.module.followup.controller;

import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.followup.dto.FollowUpSubmitDTO;
import com.wanderingpet.module.followup.service.FollowUpService;
import com.wanderingpet.module.followup.vo.FollowUpVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 回访接口（领养人）
 */
@Tag(name = "回访（领养人）", description = "填写回访表、我的回访")
@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @Operation(summary = "填写回访表")
    @PostMapping("/{id}")
    public R<Void> submit(@PathVariable Long id, @RequestBody FollowUpSubmitDTO dto) {
        followUpService.submit(id, dto);
        return R.ok();
    }

    @Operation(summary = "我的回访任务")
    @GetMapping("/my")
    public R<PageResult<FollowUpVO>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String status) {
        return R.ok(followUpService.pageMy(pageNum, pageSize, status));
    }
}
