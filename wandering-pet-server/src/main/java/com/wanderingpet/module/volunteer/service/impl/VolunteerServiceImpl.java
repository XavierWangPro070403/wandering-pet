package com.wanderingpet.module.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.enums.VolunteerStatus;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import com.wanderingpet.module.volunteer.dto.VolunteerApplyDTO;
import com.wanderingpet.module.volunteer.dto.VolunteerAuditDTO;
import com.wanderingpet.module.volunteer.entity.VolunteerApplication;
import com.wanderingpet.module.volunteer.mapper.VolunteerApplicationMapper;
import com.wanderingpet.module.volunteer.service.VolunteerService;
import com.wanderingpet.module.volunteer.vo.VolunteerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 志愿者服务实现
 */
@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl implements VolunteerService {

    private final VolunteerApplicationMapper volunteerMapper;
    private final ShelterMapper shelterMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    private LoginUser requireUser() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    private LoginUser requireShelter() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isShelter() || user.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return user;
    }

    @Override
    public Long apply(VolunteerApplyDTO dto) {
        LoginUser user = requireUser();
        Shelter shelter = shelterMapper.selectById(dto.getShelterId());
        if (shelter == null || shelter.getAuditStatus() != BizConstant.SHELTER_AUDIT_PASS) {
            throw BizException.of(ResultCode.NOT_FOUND, "救助站不存在或未入驻");
        }
        VolunteerApplication application = new VolunteerApplication();
        BeanUtils.copyProperties(dto, application);
        application.setUserId(user.getUserId());
        application.setStatus(VolunteerStatus.PENDING.name());
        application.setAuditRemark("");
        volunteerMapper.insert(application);
        return application.getId();
    }

    @Override
    public PageResult<VolunteerVO> pageMy(Integer pageNum, Integer pageSize) {
        LoginUser user = requireUser();
        Page<VolunteerApplication> page = volunteerMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<VolunteerApplication>()
                        .eq(VolunteerApplication::getUserId, user.getUserId())
                        .orderByDesc(VolunteerApplication::getCreateTime));
        List<VolunteerVO> list = page.getRecords().stream().map(item -> toVO(item, true)).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public PageResult<VolunteerVO> pageShelter(Integer pageNum, Integer pageSize, String status) {
        LoginUser user = requireShelter();
        Page<VolunteerApplication> page = volunteerMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<VolunteerApplication>()
                        .eq(VolunteerApplication::getShelterId, user.getShelterId())
                        .eq(status != null && !status.isBlank(), VolunteerApplication::getStatus, status)
                        .orderByDesc(VolunteerApplication::getCreateTime));
        List<VolunteerVO> list = page.getRecords().stream().map(item -> toVO(item, false)).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public void audit(Long id, VolunteerAuditDTO dto) {
        LoginUser user = requireShelter();
        VolunteerApplication application = volunteerMapper.selectById(id);
        if (application == null || !Objects.equals(application.getShelterId(), user.getShelterId())) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        if (!VolunteerStatus.PENDING.name().equals(application.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "该报名已审核");
        }
        application.setStatus(Boolean.TRUE.equals(dto.getPass())
                ? VolunteerStatus.APPROVED.name() : VolunteerStatus.REJECTED.name());
        application.setAuditRemark(dto.getRemark() == null ? "" : dto.getRemark());
        application.setAuditTime(LocalDateTime.now());
        volunteerMapper.updateById(application);

        String shelterName = shelterName(application.getShelterId());
        notificationService.send(application.getUserId(), AuthConstant.USER_TYPE_USER,
                Boolean.TRUE.equals(dto.getPass()) ? "志愿者报名已通过" : "志愿者报名未通过",
                Boolean.TRUE.equals(dto.getPass())
                        ? "您在 %s 的志愿者报名已通过，请留意机构后续通知。".formatted(shelterName)
                        : "您在 %s 的志愿者报名未通过：%s".formatted(shelterName, application.getAuditRemark()),
                BizConstant.BIZ_SYSTEM, application.getId());
    }

    private VolunteerVO toVO(VolunteerApplication item, boolean ownerView) {
        VolunteerVO vo = new VolunteerVO();
        BeanUtils.copyProperties(item, vo);
        vo.setAuditTime(item.getAuditTime() == null ? null : item.getAuditTime().toString().replace('T', ' '));
        vo.setCreateTime(item.getCreateTime() == null ? null : item.getCreateTime().toString().replace('T', ' '));
        Shelter shelter = shelterMapper.selectById(item.getShelterId());
        vo.setShelterName(shelter == null ? null : shelter.getName());
        if (!ownerView) {
            User applicant = userMapper.selectById(item.getUserId());
            vo.setUserNickname(applicant == null ? null : applicant.getNickname());
        }
        return vo;
    }

    private String shelterName(Long shelterId) {
        Shelter shelter = shelterMapper.selectById(shelterId);
        return shelter == null ? "" : shelter.getName();
    }
}
