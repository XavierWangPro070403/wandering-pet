package com.wanderingpet.module.donation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.MaskUtil;
import com.wanderingpet.common.util.SeqUtil;
import com.wanderingpet.module.donation.dto.DonateDTO;
import com.wanderingpet.module.donation.dto.ProjectSaveDTO;
import com.wanderingpet.module.donation.entity.DonationProject;
import com.wanderingpet.module.donation.entity.DonationRecord;
import com.wanderingpet.module.donation.mapper.DonationProjectMapper;
import com.wanderingpet.module.donation.mapper.DonationRecordMapper;
import com.wanderingpet.module.donation.service.DonationService;
import com.wanderingpet.module.donation.vo.DonationRecordVO;
import com.wanderingpet.module.donation.vo.DonorRankVO;
import com.wanderingpet.module.donation.vo.ProjectVO;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 募捐服务实现（模拟支付：捐赠即成功，真实支付在 10.1 安全要求外另行接入）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationProjectMapper projectMapper;
    private final DonationRecordMapper recordMapper;
    private final ShelterMapper shelterMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final SeqUtil seqUtil;

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
    public List<ProjectVO> listOngoing() {
        return projectMapper.selectList(new LambdaQueryWrapper<DonationProject>()
                        .eq(DonationProject::getStatus, "ONGOING")
                        .orderByDesc(DonationProject::getCreateTime)).stream()
                .map(this::toVO).toList();
    }

    @Override
    public ProjectVO detail(Long id) {
        DonationProject project = projectMapper.selectById(id);
        if (project == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        return toVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void donate(Long projectId, DonateDTO dto) {
        LoginUser user = requireUser();
        DonationProject project = projectMapper.selectById(projectId);
        if (project == null || !"ONGOING".equals(project.getStatus())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "项目不存在或已结束");
        }
        // 模拟支付：生成支付单号，直接成功
        DonationRecord record = new DonationRecord();
        record.setProjectId(projectId);
        record.setUserId(user.getUserId());
        String nickname = userNickname(user.getUserId());
        record.setDonorName(Boolean.TRUE.equals(dto.getIsAnonymous()) || dto.getIsAnonymous() != null && dto.getIsAnonymous() == 1
                ? "匿名爱心人士" : nickname);
        record.setAmount(dto.getAmount());
        record.setPayNo(seqUtil.next(BizConstant.SEQ_PAY, "PAY"));
        record.setPayStatus("SUCCESS");
        record.setIsAnonymous(Boolean.TRUE.equals(dto.getIsAnonymous()) || Integer.valueOf(1).equals(dto.getIsAnonymous()) ? 1 : 0);
        record.setMessage(dto.getMessage() == null ? "" : dto.getMessage());
        record.setPayTime(LocalDateTime.now());
        recordMapper.insert(record);

        // 更新项目进度
        BigDecimal raised = project.getRaisedAmount() == null ? BigDecimal.ZERO : project.getRaisedAmount();
        project.setRaisedAmount(raised.add(dto.getAmount()));
        project.setDonorCount(project.getDonorCount() == null ? 1 : project.getDonorCount() + 1);
        projectMapper.updateById(project);

        // 通知机构
        notificationService.send(project.getShelterId(), AuthConstant.USER_TYPE_SHELTER,
                "收到新的捐赠", "项目「%s」收到一笔 ¥%s 捐赠（%s）。".formatted(project.getTitle(), dto.getAmount(), record.getDonorName()),
                BizConstant.BIZ_DONATION, record.getId());
    }

    @Override
    public List<DonationRecordVO> recordsOf(Long projectId, Integer limit) {
        List<DonationRecord> records = recordMapper.selectList(new LambdaQueryWrapper<DonationRecord>()
                .eq(DonationRecord::getProjectId, projectId)
                .eq(DonationRecord::getPayStatus, "SUCCESS")
                .orderByDesc(DonationRecord::getPayTime)
                .last("LIMIT " + Math.min(limit == null ? 20 : limit, 100)));
        return records.stream().map(this::toRecordVO).toList();
    }

    @Override
    public List<DonorRankVO> donorRank(Long projectId, Integer topN) {
        return recordMapper.selectDonorRank(projectId, Math.min(topN == null ? 10 : topN, 50));
    }

    // ---------------------------------------------------------------- 机构端

    @Override
    public List<ProjectVO> myProjects() {
        LoginUser user = requireShelter();
        return projectMapper.selectList(new LambdaQueryWrapper<DonationProject>()
                        .eq(DonationProject::getShelterId, user.getShelterId())
                        .orderByDesc(DonationProject::getCreateTime)).stream()
                .map(this::toVO).toList();
    }

    @Override
    public Long saveProject(ProjectSaveDTO dto) {
        LoginUser user = requireShelter();
        DonationProject project = new DonationProject();
        applyDto(project, dto);
        project.setShelterId(user.getShelterId());
        project.setRaisedAmount(BigDecimal.ZERO);
        project.setDonorCount(0);
        project.setStatus("ONGOING");
        projectMapper.insert(project);
        return project.getId();
    }

    @Override
    public void updateProject(Long id, ProjectSaveDTO dto) {
        LoginUser user = requireShelter();
        DonationProject project = requireOwnedProject(id, user);
        applyDto(project, dto);
        projectMapper.updateById(project);
    }

    @Override
    public void closeProject(Long id) {
        LoginUser user = requireShelter();
        DonationProject project = requireOwnedProject(id, user);
        project.setStatus("CLOSED");
        projectMapper.updateById(project);
    }

    @Override
    public PageResult<DonationRecordVO> myRecords(Integer pageNum, Integer pageSize, Long projectId) {
        LoginUser user = requireShelter();
        List<Long> projectIds = projectMapper.selectList(new LambdaQueryWrapper<DonationProject>()
                        .eq(DonationProject::getShelterId, user.getShelterId()).select(DonationProject::getId))
                .stream().map(DonationProject::getId).toList();
        if (projectIds.isEmpty()) {
            return new PageResult<>(List.of(), 0, pageNum, pageSize, 0);
        }
        Page<DonationRecord> page = recordMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<DonationRecord>()
                        .in(DonationRecord::getProjectId, projectIds)
                        .eq(projectId != null, DonationRecord::getProjectId, projectId)
                        .orderByDesc(DonationRecord::getPayTime));
        List<DonationRecordVO> list = page.getRecords().stream().map(this::toRecordVO).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public PageResult<DonationRecordVO> pageAdminRecords(Integer pageNum, Integer pageSize, Long projectId) {
        Page<DonationRecord> page = recordMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<DonationRecord>()
                        .eq(projectId != null, DonationRecord::getProjectId, projectId)
                        .orderByDesc(DonationRecord::getPayTime));
        List<DonationRecordVO> list = page.getRecords().stream().map(this::toRecordVO).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    // ---------------------------------------------------------------- private

    private void applyDto(DonationProject project, ProjectSaveDTO dto) {
        project.setTitle(dto.getTitle());
        project.setCover(dto.getCover());
        project.setTargetAmount(dto.getTargetAmount());
        project.setStartDate(dto.getStartDate() == null || dto.getStartDate().isBlank()
                ? LocalDate.now() : LocalDate.parse(dto.getStartDate()));
        project.setEndDate(dto.getEndDate() == null || dto.getEndDate().isBlank()
                ? LocalDate.now().plusMonths(1) : LocalDate.parse(dto.getEndDate()));
        project.setContent(dto.getContent() == null ? "" : dto.getContent());
    }

    private DonationProject requireOwnedProject(Long id, LoginUser user) {
        DonationProject project = projectMapper.selectById(id);
        if (project == null || !Objects.equals(project.getShelterId(), user.getShelterId())) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        return project;
    }

    private String userNickname(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? "爱心人士" : user.getNickname();
    }

    private ProjectVO toVO(DonationProject project) {
        ProjectVO vo = new ProjectVO();
        org.springframework.beans.BeanUtils.copyProperties(project, vo);
        vo.setStartDate(project.getStartDate() == null ? null : project.getStartDate().toString());
        vo.setEndDate(project.getEndDate() == null ? null : project.getEndDate().toString());
        vo.setCreateTime(project.getCreateTime() == null ? null : project.getCreateTime().toString().replace('T', ' '));
        Shelter shelter = shelterMapper.selectById(project.getShelterId());
        if (shelter != null) {
            vo.setShelterName(shelter.getName());
            vo.setShelterCity(shelter.getCity());
        }
        BigDecimal target = project.getTargetAmount() == null ? BigDecimal.ONE : project.getTargetAmount();
        BigDecimal raised = project.getRaisedAmount() == null ? BigDecimal.ZERO : project.getRaisedAmount();
        vo.setProgress(raised.multiply(BigDecimal.valueOf(100)).divide(target, 0, RoundingMode.DOWN).intValue());
        return vo;
    }

    private DonationRecordVO toRecordVO(DonationRecord record) {
        DonationRecordVO vo = new DonationRecordVO();
        org.springframework.beans.BeanUtils.copyProperties(record, vo);
        vo.setDonorName(Integer.valueOf(1).equals(record.getIsAnonymous())
                ? "匿名爱心人士" : MaskUtil.maskName(record.getDonorName()));
        vo.setPayTime(record.getPayTime() == null ? null : record.getPayTime().toString().replace('T', ' '));
        return vo;
    }
}
