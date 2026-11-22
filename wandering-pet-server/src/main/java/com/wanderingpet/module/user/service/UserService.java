package com.wanderingpet.module.user.service;

import com.wanderingpet.module.user.dto.AddressDTO;
import com.wanderingpet.module.user.dto.RealNameDTO;
import com.wanderingpet.module.user.dto.UpdateAdopterProfileDTO;
import com.wanderingpet.module.user.dto.UpdateProfileDTO;
import com.wanderingpet.module.user.vo.AdopterProfileVO;
import com.wanderingpet.module.user.vo.AddressVO;
import com.wanderingpet.module.user.vo.UserProfileVO;
import com.wanderingpet.module.user.vo.UserSummaryVO;

import java.util.List;

/**
 * C 端用户服务
 */
public interface UserService {

    /** 当前用户资料（手机号脱敏） */
    UserProfileVO myProfile();

    /** 更新资料 */
    void updateProfile(UpdateProfileDTO dto);

    /** 领养资料（本人视角，申请领养时自动填充） */
    AdopterProfileVO adopterProfile();

    /** 保存领养资料（补全） */
    void updateAdopterProfile(UpdateAdopterProfileDTO dto);

    /** 实名认证（身份证 AES 加密入库） */
    void realName(RealNameDTO dto);

    /** 数字摘要（我的页面） */
    UserSummaryVO mySummary();

    // ---------- 地址 ----------

    List<AddressVO> myAddresses();

    Long saveAddress(AddressDTO dto);

    void updateAddress(Long id, AddressDTO dto);

    void deleteAddress(Long id);
}
