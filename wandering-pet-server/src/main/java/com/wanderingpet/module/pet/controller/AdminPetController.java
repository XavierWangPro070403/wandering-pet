package com.wanderingpet.module.pet.controller;

import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.pet.dto.PetQueryDTO;
import com.wanderingpet.module.pet.service.PetService;
import com.wanderingpet.module.pet.vo.AdminPetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端宠物全量接口
 */
@Tag(name = "管理端-宠物全量", description = "全平台宠物档案管理（仅管理员）")
@RestController
@RequestMapping("/api/v1/admin/pets")
@RequiredArgsConstructor
public class AdminPetController {

    private final PetService petService;

    @Operation(summary = "宠物全量分页")
    @RequirePermission("pet:manage-all")
    @GetMapping
    public R<PageResult<AdminPetVO>> page(PetQueryDTO query) {
        return R.ok(petService.pageAdmin(query));
    }
}
