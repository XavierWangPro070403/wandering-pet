package com.wanderingpet.module.rescue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.enums.ReportStatus;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.SeqUtil;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.PetCategory;
import com.wanderingpet.module.pet.mapper.PetCategoryMapper;
import com.wanderingpet.module.rescue.dto.ReportHandleDTO;
import com.wanderingpet.module.rescue.dto.ReportQueryDTO;
import com.wanderingpet.module.rescue.dto.RescueReportDTO;
import com.wanderingpet.module.rescue.entity.RescueReport;
import com.wanderingpet.module.rescue.mapper.RescueReportMapper;
import com.wanderingpet.module.rescue.service.RescueService;
import com.wanderingpet.module.rescue.vo.AdminRescueVO;
import com.wanderingpet.module.sys.entity.SysUser;
import com.wanderingpet.module.sys.mapper.SysUserMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 流浪动物线索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RescueServiceImpl implements RescueService {

    private final RescueReportMapper rescueReportMapper;
    private final PetCategoryMapper petCategoryMapper;
    private final SysUserMapper sysUserMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final SeqUtil seqUtil;
    private final ObjectMapper objectMapper;

    private LoginUser requireUser() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    public Long report(RescueReportDTO dto) {
        LoginUser user = requireUser();
        if (dto.getImages() != null && dto.getImages().size() > 9) {
            throw BizException.of(ResultCode.BAD_REQUEST, "现场图片最多 9 张");
        }
        RescueReport report = new RescueReport();
        report.setReportNo(seqUtil.next(BizConstant.SEQ_REPORT, "RSR"));
        report.setUserId(user.getUserId());
        report.setCategoryId(dto.getCategoryId());
        report.setDescription(dto.getDescription());
        report.setImages(toJson(dto.getImages()));
        report.setProvince(dto.getProvince());
        report.setCity(dto.getCity());
        report.setDistrict(dto.getDistrict() == null ? "" : dto.getDistrict());
        report.setAddress(dto.getAddress());
        report.setLongitude(dto.getLongitude());
        report.setLatitude(dto.getLatitude());
        report.setUrgency(dto.getUrgency() == null ? 2 : dto.getUrgency());
        report.setStatus(ReportStatus.PENDING.name());
        report.setHandleRemark("");
        rescueReportMapper.insert(report);
        return report.getId();
    }

    @Override
    public PageResult<AdminRescueVO> pageMy(ReportQueryDTO query) {
        LoginUser user = requireUser();
        Page<RescueReport> page = rescueReportMapper.selectPage(new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)),
                new LambdaQueryWrapper<RescueReport>()
                        .eq(RescueReport::getUserId, user.getUserId())
                        .eq(query.getStatus() != null && !query.getStatus().isBlank(), RescueReport::getStatus, query.getStatus())
                        .orderByDesc(RescueReport::getCreateTime));
        List<AdminRescueVO> list = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public PageResult<AdminRescueVO> pageAdmin(ReportQueryDTO query) {
        Page<AdminRescueVO> page = rescueReportMapper.selectAdminPage(
                new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)), query);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long id, ReportHandleDTO dto) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isAdmin()) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        RescueReport report = requireReport(id);
        if (!ReportStatus.PENDING.name().equals(report.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "仅待处理线索可以分派");
        }
        SysUser handler = sysUserMapper.selectById(dto.getHandlerId());
        if (handler == null || !AuthConstant.USER_TYPE_SHELTER.equals(handler.getRoleCode())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "处理方必须是救助站账号");
        }
        report.setStatus(ReportStatus.ASSIGNED.name());
        report.setHandlerId(handler.getId());
        report.setHandleRemark(dto.getRemark() == null ? "" : dto.getRemark());
        report.setHandleTime(LocalDateTime.now());
        rescueReportMapper.updateById(report);
        notifyHandler(report, handler, "新的线索待处理", "线索 %s 已分派给您，请尽快跟进。".formatted(report.getReportNo()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(Long id, ReportHandleDTO dto) {
        RescueReport report = requireHandlerReport(id);
        if (!ReportStatus.ASSIGNED.name().equals(report.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "仅已分派线索可以开始处理");
        }
        report.setStatus(ReportStatus.PROCESSING.name());
        report.setHandleRemark(dto.getRemark() == null ? "" : dto.getRemark());
        report.setHandleTime(LocalDateTime.now());
        rescueReportMapper.updateById(report);
        notifyReporter(report, "线索处理中", "您上报的线索 %s 正在由救助站处理。".formatted(report.getReportNo()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long id, ReportHandleDTO dto) {
        RescueReport report = requireHandlerReport(id);
        if (!ReportStatus.PROCESSING.name().equals(report.getStatus())
                && !ReportStatus.ASSIGNED.name().equals(report.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "当前状态不能标记解决");
        }
        report.setStatus(ReportStatus.RESOLVED.name());
        report.setHandleRemark(dto.getRemark() == null ? "" : dto.getRemark());
        report.setHandleTime(LocalDateTime.now());
        rescueReportMapper.updateById(report);
        notifyReporter(report, "线索已解决", "您上报的线索 %s 已处理完成，感谢您的反馈。".formatted(report.getReportNo()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isAdmin()) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        RescueReport report = requireReport(id);
        if (!ReportStatus.RESOLVED.name().equals(report.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "仅已解决的线索可以关闭");
        }
        report.setStatus(ReportStatus.CLOSED.name());
        report.setHandleTime(LocalDateTime.now());
        rescueReportMapper.updateById(report);
    }

    // ---------------------------------------------------------------- private

    private RescueReport requireReport(Long id) {
        RescueReport report = rescueReportMapper.selectById(id);
        if (report == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        return report;
    }

    private RescueReport requireHandlerReport(Long id) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isShelter()) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        RescueReport report = requireReport(id);
        if (!Objects.equals(report.getHandlerId(), user.getUserId())) {
            throw BizException.of(ResultCode.FORBIDDEN, "该线索未分派给当前账号");
        }
        return report;
    }

    private void notifyHandler(RescueReport report, SysUser handler, String title, String content) {
        notificationService.send(handler.getId(), AuthConstant.USER_TYPE_SHELTER, title, content,
                BizConstant.BIZ_REPORT, report.getId());
    }

    private void notifyReporter(RescueReport report, String title, String content) {
        notificationService.send(report.getUserId(), AuthConstant.USER_TYPE_USER, title, content,
                BizConstant.BIZ_REPORT, report.getId());
    }

    private AdminRescueVO toVO(RescueReport report) {
        AdminRescueVO vo = new AdminRescueVO();
        org.springframework.beans.BeanUtils.copyProperties(report, vo);
        vo.setImages(parseImages(report.getImages()));
        vo.setHandleTime(report.getHandleTime() == null ? null : report.getHandleTime().toString().replace('T', ' '));
        vo.setCreateTime(report.getCreateTime() == null ? null : report.getCreateTime().toString().replace('T', ' '));
        if (report.getCategoryId() != null) {
            PetCategory category = petCategoryMapper.selectById(report.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getName());
        }
        if (report.getHandlerId() != null) {
            SysUser handler = sysUserMapper.selectById(report.getHandlerId());
            vo.setHandlerName(handler == null ? null : handler.getRealName());
        }
        User reporter = userMapper.selectById(report.getUserId());
        vo.setUserNickname(reporter == null ? null : reporter.getNickname());
        return vo;
    }

    private String toJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() { });
        } catch (Exception e) {
            return List.of();
        }
    }
}
