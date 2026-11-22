package com.wanderingpet.module.pet.controller;

import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.pet.dto.PetQueryDTO;
import com.wanderingpet.module.pet.service.CategoryService;
import com.wanderingpet.module.pet.service.FavoriteService;
import com.wanderingpet.module.pet.service.PetService;
import com.wanderingpet.module.pet.vo.BreedVO;
import com.wanderingpet.module.pet.vo.CategoryVO;
import com.wanderingpet.module.pet.vo.DynamicVO;
import com.wanderingpet.module.pet.vo.PetDetailVO;
import com.wanderingpet.module.pet.vo.PetListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门户/小程序宠物接口（游客可浏览）
 */
@Tag(name = "宠物（门户）", description = "宠物列表、详情、相似推荐、分类品种、动态")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final CategoryService categoryService;
    private final FavoriteService favoriteService;

    @Operation(summary = "宠物分页列表")
    @GetMapping("/pets")
    public R<PageResult<PetListVO>> page(PetQueryDTO query) {
        return R.ok(petService.pagePortal(query));
    }

    @Operation(summary = "宠物详情")
    @GetMapping("/pets/{id}")
    public R<PetDetailVO> detail(@PathVariable Long id) {
        return R.ok(petService.detail(id));
    }

    @Operation(summary = "相似推荐")
    @GetMapping("/pets/{id}/similar")
    public R<List<PetListVO>> similar(@PathVariable Long id,
                                      @Parameter(description = "数量，默认 4") @RequestParam(required = false) Integer limit) {
        return R.ok(petService.similar(id, limit));
    }

    @Operation(summary = "记录一次浏览")
    @PostMapping("/pets/{id}/view")
    public R<Void> view(@PathVariable Long id) {
        petService.recordView(id);
        return R.ok();
    }

    @Operation(summary = "宠物动态（MongoDB）")
    @GetMapping("/pets/{id}/dynamics")
    public R<List<DynamicVO>> dynamics(@PathVariable Long id,
                                       @RequestParam(required = false) Integer limit) {
        return R.ok(favoriteService.listDynamics(id, limit));
    }

    @Operation(summary = "物种分类列表")
    @GetMapping("/pet-categories")
    public R<List<CategoryVO>> categories() {
        return R.ok(categoryService.listCategories());
    }

    @Operation(summary = "品种列表")
    @GetMapping("/pet-breeds")
    public R<List<BreedVO>> breeds(@Parameter(description = "物种 ID，可空") @RequestParam(required = false) Long categoryId) {
        return R.ok(categoryService.listBreeds(categoryId));
    }
}
