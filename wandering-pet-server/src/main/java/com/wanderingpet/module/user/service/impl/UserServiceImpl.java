package com.wanderingpet.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.MaskUtil;
import com.wanderingpet.module.adoption.entity.AdoptionApplication;
import com.wanderingpet.module.adoption.mapper.AdoptionApplicationMapper;
import com.wanderingpet.module.chat.entity.ChatSession;
import com.wanderingpet.module.chat.mapper.ChatSessionMapper;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.Favorite;
import com.wanderingpet.module.pet.mapper.FavoriteMapper;
import com.wanderingpet.module.rescue.entity.RescueReport;
import com.wanderingpet.module.rescue.mapper.RescueReportMapper;
import com.wanderingpet.module.user.dto.AddressDTO;
import com.wanderingpet.module.user.dto.RealNameDTO;
import com.wanderingpet.module.user.dto.UpdateAdopterProfileDTO;
import com.wanderingpet.module.user.dto.UpdateProfileDTO;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.entity.UserAddress;
import com.wanderingpet.module.user.mapper.UserAddressMapper;
import com.wanderingpet.module.user.mapper.UserMapper;
import com.wanderingpet.module.user.service.UserService;
import com.wanderingpet.module.user.vo.AdopterProfileVO;
import com.wanderingpet.module.user.vo.AddressVO;
import com.wanderingpet.module.user.vo.UserProfileVO;
import com.wanderingpet.module.user.vo.UserSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * C 端用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;
    private final FavoriteMapper favoriteMapper;
    private final AdoptionApplicationMapper applicationMapper;
    private final RescueReportMapper rescueReportMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final NotificationService notificationService;

    private LoginUser requireUser() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    private User requireMe() {
        User user = userMapper.selectById(requireUser().getUserId());
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public UserProfileVO myProfile() {
        User user = requireMe();
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        vo.setPhone(user.getPhone() == null ? null : MaskUtil.maskPhone(user.getPhone()));
        vo.setWechatBound(user.getOpenid() != null && !user.getOpenid().isBlank());
        vo.setPoints(user.getPoints() == null ? 0 : user.getPoints());
        return vo;
    }

    @Override
    public AdopterProfileVO adopterProfile() {
        User user = requireMe();
        AdopterProfileVO vo = new AdopterProfileVO();
        // 默认姓名：实名姓名 > 领养资料姓名 > 昵称
        vo.setAdopterName(firstNotBlank(user.getRealName(), user.getAdopterName(), user.getNickname()));
        vo.setPhone(user.getPhone() == null ? "" : user.getPhone());
        vo.setCity(user.getCity());
        vo.setAdopterAge(user.getAdopterAge());
        vo.setOccupation(user.getOccupation());
        vo.setHousingType(user.getHousingType());
        vo.setHasPetExperience(user.getHasPetExperience());
        vo.setFamilyAgree(user.getFamilyAgree());
        vo.setHasChildren(user.getHasChildren());
        vo.setHasAllergy(user.getHasAllergy());
        vo.setComplete(firstNotBlank(user.getRealName(), user.getAdopterName(), user.getNickname()) != null
                && user.getPhone() != null
                && user.getCity() != null && !user.getCity().isBlank()
                && user.getAdopterAge() != null
                && user.getHousingType() != null && !user.getHousingType().isBlank()
                && user.getHasPetExperience() != null
                && user.getFamilyAgree() != null);
        return vo;
    }

    @Override
    public void updateAdopterProfile(UpdateAdopterProfileDTO dto) {
        User user = requireMe();
        if (notBlank(dto.getAdopterName())) user.setAdopterName(dto.getAdopterName());
        if (notBlank(dto.getCity())) user.setCity(dto.getCity());
        if (dto.getAdopterAge() != null) user.setAdopterAge(dto.getAdopterAge());
        if (notBlank(dto.getOccupation())) user.setOccupation(dto.getOccupation());
        if (notBlank(dto.getHousingType())) user.setHousingType(dto.getHousingType());
        if (dto.getHasPetExperience() != null) user.setHasPetExperience(dto.getHasPetExperience());
        if (dto.getFamilyAgree() != null) user.setFamilyAgree(dto.getFamilyAgree());
        if (dto.getHasChildren() != null) user.setHasChildren(dto.getHasChildren());
        if (dto.getHasAllergy() != null) user.setHasAllergy(dto.getHasAllergy());
        userMapper.updateById(user);
    }

    private String firstNotBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private boolean notBlank(String v) {
        return v != null && !v.isBlank();
    }

    @Override
    public void updateProfile(UpdateProfileDTO dto) {
        User user = requireMe();
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        userMapper.updateById(user);
    }

    @Override
    public void realName(RealNameDTO dto) {
        User user = requireMe();
        user.setRealName(dto.getRealName());
        // TypeHandler 自动加密
        user.setIdCardNo(dto.getIdCardNo());
        user.setRealNameStatus(1);
        userMapper.updateById(user);
    }

    @Override
    public UserSummaryVO mySummary() {
        LoginUser current = requireUser();
        Long userId = current.getUserId();
        Long applications = applicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getUserId, userId));
        Long activeApplications = applicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getUserId, userId)
                .in(AdoptionApplication::getStatus, "SUBMITTED", "REVIEWING", "INTERVIEW", "HOME_VISIT", "APPROVED", "SIGNED"));
        Long favorites = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId));
        Long reports = rescueReportMapper.selectCount(new LambdaQueryWrapper<RescueReport>()
                .eq(RescueReport::getUserId, userId));
        return new UserSummaryVO(applications, activeApplications, favorites, reports, notificationService.unreadCount());
    }

    @Override
    public List<AddressVO> myAddresses() {
        LoginUser current = requireUser();
        List<UserAddress> list = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, current.getUserId())
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime));
        return list.stream().map(item -> {
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAddress(AddressDTO dto) {
        LoginUser current = requireUser();
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(current.getUserId());
        if (Boolean.TRUE.equals(dto.getIsDefault()) || Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(current.getUserId());
        } else {
            address.setIsDefault(0);
        }
        userAddressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long id, AddressDTO dto) {
        LoginUser current = requireUser();
        UserAddress address = requireOwnedAddress(id, current);
        BeanUtils.copyProperties(dto, address);
        address.setId(id);
        if (Boolean.TRUE.equals(dto.getIsDefault()) || Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(current.getUserId());
        } else {
            address.setIsDefault(Objects.requireNonNullElse(address.getIsDefault(), 0));
        }
        userAddressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Long id) {
        LoginUser current = requireUser();
        requireOwnedAddress(id, current);
        userAddressMapper.deleteById(id);
    }

    private UserAddress requireOwnedAddress(Long id, LoginUser current) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !Objects.equals(address.getUserId(), current.getUserId())) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        return address;
    }

    private void clearDefault(Long userId) {
        userAddressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0));
    }
}
