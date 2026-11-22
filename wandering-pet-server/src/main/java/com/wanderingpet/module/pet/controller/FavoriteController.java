package com.wanderingpet.module.pet.controller;

import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.pet.dto.FavoriteDTO;
import com.wanderingpet.module.pet.service.FavoriteService;
import com.wanderingpet.module.pet.vo.FavoritePetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏接口（USER）
 */
@Tag(name = "收藏", description = "收藏/取消收藏/我的收藏")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "收藏")
    @PostMapping
    public R<Void> favorite(@Valid @RequestBody FavoriteDTO dto) {
        favoriteService.favorite(dto);
        return R.ok();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{targetType}/{targetId}")
    public R<Void> unfavorite(@PathVariable String targetType, @PathVariable Long targetId) {
        favoriteService.unfavorite(targetType, targetId);
        return R.ok();
    }

    @Operation(summary = "我的宠物收藏")
    @GetMapping("/my")
    public R<PageResult<FavoritePetVO>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(favoriteService.pageMyPetFavorites(pageNum, pageSize));
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/status")
    public R<Boolean> status(@RequestParam String targetType, @RequestParam Long targetId) {
        return R.ok(favoriteService.isFavorited(targetType, targetId));
    }
}
