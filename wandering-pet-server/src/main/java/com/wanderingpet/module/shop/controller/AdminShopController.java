package com.wanderingpet.module.shop.controller;

import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.shop.dto.ProductSaveDTO;
import com.wanderingpet.module.shop.service.ShopService;
import com.wanderingpet.module.shop.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城管理端（平台管理员）：商品管理与发货
 * 路径位于 /api/v1/admin/**，由鉴权拦截器保证仅 ADMIN 可访问
 */
@Tag(name = "商城管理", description = "商品管理、订单发货（平台管理员）")
@RestController
@RequestMapping("/api/v1/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    @Operation(summary = "商品分页（全状态）")
    @GetMapping("/products")
    public R<PageResult<ProductVO>> products(@RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(shopService.adminPageProducts(keyword, pageNum, pageSize));
    }

    @Operation(summary = "新增商品")
    @PostMapping("/products")
    public R<Long> save(@Valid @RequestBody ProductSaveDTO dto) {
        return R.ok(shopService.saveProduct(dto));
    }

    @Operation(summary = "编辑商品")
    @PutMapping("/products/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        shopService.updateProduct(id, dto);
        return R.ok();
    }

    @Operation(summary = "商品上/下架")
    @PostMapping("/products/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        shopService.changeProductStatus(id, body.get("status"));
        return R.ok();
    }

    @Operation(summary = "订单发货（已支付 → 已发货）")
    @PostMapping("/orders/{id}/ship")
    public R<Void> ship(@PathVariable Long id) {
        shopService.ship(id);
        return R.ok();
    }
}
