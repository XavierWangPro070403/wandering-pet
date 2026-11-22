package com.wanderingpet.module.sys.controller;

import com.wanderingpet.common.result.R;
import com.wanderingpet.module.sys.service.SysManageService;
import com.wanderingpet.module.sys.vo.DictItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据字典公开接口（供三端下拉选项）
 */
@Tag(name = "数据字典", description = "字典全量（按类型分组，Redis 缓存 24h）")
@RestController
@RequestMapping("/api/v1/dicts")
@RequiredArgsConstructor
public class DictController {

    private final SysManageService sysManageService;

    @Operation(summary = "字典全量")
    @GetMapping
    public R<Map<String, List<DictItemVO>>> all() {
        return R.ok(sysManageService.dictAll());
    }
}
