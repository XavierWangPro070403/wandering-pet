package com.wanderingpet.module.pet.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.pet.dto.HealthRecordDTO;
import com.wanderingpet.module.pet.dto.PetQueryDTO;
import com.wanderingpet.module.pet.dto.PetSaveDTO;
import com.wanderingpet.module.pet.dto.PetStatusDTO;
import com.wanderingpet.module.pet.service.CategoryService;
import com.wanderingpet.module.pet.service.FavoriteService;
import com.wanderingpet.module.pet.service.PetService;
import com.wanderingpet.module.pet.vo.DynamicVO;
import com.wanderingpet.module.pet.vo.HealthRecordVO;
import com.wanderingpet.module.pet.vo.PetManageListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 机构端宠物接口（SHELTER）
 */
@Tag(name = "机构端-宠物管理", description = "宠物发布、编辑、上下架、健康档案、动态")
@RestController
@RequestMapping("/api/v1/shelter/pets")
@RequiredArgsConstructor
public class ShelterPetController {

    private final PetService petService;
    private final CategoryService categoryService;
    private final FavoriteService favoriteService;

    @Operation(summary = "本机构宠物分页（全状态）")
    @RequirePermission("shelter:pet")
    @GetMapping
    public R<PageResult<PetManageListVO>> page(PetQueryDTO query) {
        return R.ok(petService.pageShelter(query));
    }

    @Operation(summary = "发布宠物档案")
    @RequirePermission("pet:create")
    @PostMapping
    @OpLog(module = "pet", action = "发布宠物档案")
    public R<Long> save(@Valid @RequestBody PetSaveDTO dto) {
        return R.ok(petService.save(dto));
    }

    @Operation(summary = "编辑宠物档案")
    @RequirePermission("pet:update")
    @PutMapping("/{id}")
    @OpLog(module = "pet", action = "编辑宠物档案")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PetSaveDTO dto) {
        petService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除宠物档案")
    @RequirePermission("pet:delete")
    @DeleteMapping("/{id}")
    @OpLog(module = "pet", action = "删除宠物档案")
    public R<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return R.ok();
    }

    @Operation(summary = "上下架/状态变更（支持批量）")
    @RequirePermission("pet:status")
    @PostMapping("/status")
    @OpLog(module = "pet", action = "宠物状态变更")
    public R<Void> changeStatus(@RequestBody PetStatusDTO dto) {
        petService.changeStatus(dto);
        return R.ok();
    }

    // ---------------- 健康档案 ----------------

    @Operation(summary = "健康档案列表")
    @RequirePermission("shelter:pet")
    @GetMapping("/{petId}/health-records")
    public R<List<HealthRecordVO>> healthRecords(@PathVariable Long petId) {
        return R.ok(categoryService.healthRecords(petId));
    }

    @Operation(summary = "新增健康档案")
    @RequirePermission("health-record:manage")
    @PostMapping("/{petId}/health-records")
    @OpLog(module = "pet", action = "新增健康档案")
    public R<Long> saveHealthRecord(@PathVariable Long petId, @Valid @RequestBody HealthRecordDTO dto) {
        dto.setPetId(petId);
        return R.ok(categoryService.saveHealthRecord(dto));
    }

    @Operation(summary = "更新健康档案")
    @RequirePermission("health-record:manage")
    @PutMapping("/health-records/{id}")
    @OpLog(module = "pet", action = "更新健康档案")
    public R<Void> updateHealthRecord(@PathVariable Long id, @Valid @RequestBody HealthRecordDTO dto) {
        categoryService.updateHealthRecord(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除健康档案")
    @RequirePermission("health-record:manage")
    @DeleteMapping("/health-records/{id}")
    @OpLog(module = "pet", action = "删除健康档案")
    public R<Void> deleteHealthRecord(@PathVariable Long id) {
        categoryService.deleteHealthRecord(id);
        return R.ok();
    }

    // ---------------- 宠物动态 ----------------

    @Operation(summary = "发布宠物动态（MongoDB）")
    @RequirePermission("pet:update")
    @PostMapping("/{petId}/dynamics")
    @OpLog(module = "pet", action = "发布宠物动态")
    public R<Void> saveDynamic(@PathVariable Long petId, @RequestBody Map<String, Object> body) {
        String content = String.valueOf(body.getOrDefault("content", ""));
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) body.getOrDefault("images", List.of());
        favoriteService.saveDynamic(petId, content, images);
        return R.ok();
    }

    @Operation(summary = "宠物动态列表")
    @RequirePermission("shelter:pet")
    @GetMapping("/{petId}/dynamics")
    public R<List<DynamicVO>> dynamics(@PathVariable Long petId,
                                       @RequestParam(required = false) Integer limit) {
        return R.ok(favoriteService.listDynamics(petId, limit));
    }
}
