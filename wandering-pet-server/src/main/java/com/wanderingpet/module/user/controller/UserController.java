package com.wanderingpet.module.user.controller;

import com.wanderingpet.common.result.R;
import com.wanderingpet.module.user.dto.AddressDTO;
import com.wanderingpet.module.user.dto.RealNameDTO;
import com.wanderingpet.module.user.dto.UpdateAdopterProfileDTO;
import com.wanderingpet.module.user.dto.UpdateProfileDTO;
import com.wanderingpet.module.user.service.UserService;
import com.wanderingpet.module.user.vo.AdopterProfileVO;
import com.wanderingpet.module.user.vo.AddressVO;
import com.wanderingpet.module.user.vo.UserProfileVO;
import com.wanderingpet.module.user.vo.UserSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端用户中心接口
 */
@Tag(name = "用户中心", description = "C 端用户资料、实名认证、地址、数字摘要")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "我的资料")
    @GetMapping("/me")
    public R<UserProfileVO> myProfile() {
        return R.ok(userService.myProfile());
    }

    @Operation(summary = "更新我的资料")
    @PutMapping("/me")
    public R<Void> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        userService.updateProfile(dto);
        return R.ok();
    }

    @Operation(summary = "我的领养资料（申请领养时自动填充）")
    @GetMapping("/me/adopter-profile")
    public R<AdopterProfileVO> adopterProfile() {
        return R.ok(userService.adopterProfile());
    }

    @Operation(summary = "保存领养资料（补全）")
    @PutMapping("/me/adopter-profile")
    public R<Void> updateAdopterProfile(@RequestBody UpdateAdopterProfileDTO dto) {
        userService.updateAdopterProfile(dto);
        return R.ok();
    }

    @Operation(summary = "实名认证")
    @PostMapping("/me/real-name")
    public R<Void> realName(@Valid @RequestBody RealNameDTO dto) {
        userService.realName(dto);
        return R.ok();
    }

    @Operation(summary = "我的数字摘要")
    @GetMapping("/me/summary")
    public R<UserSummaryVO> mySummary() {
        return R.ok(userService.mySummary());
    }

    @Operation(summary = "我的地址列表")
    @GetMapping("/me/addresses")
    public R<List<AddressVO>> myAddresses() {
        return R.ok(userService.myAddresses());
    }

    @Operation(summary = "新增地址")
    @PostMapping("/me/addresses")
    public R<Long> saveAddress(@Valid @RequestBody AddressDTO dto) {
        return R.ok(userService.saveAddress(dto));
    }

    @Operation(summary = "更新地址")
    @PutMapping("/me/addresses/{id}")
    public R<Void> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        userService.updateAddress(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/me/addresses/{id}")
    public R<Void> deleteAddress(@PathVariable Long id) {
        userService.deleteAddress(id);
        return R.ok();
    }
}
