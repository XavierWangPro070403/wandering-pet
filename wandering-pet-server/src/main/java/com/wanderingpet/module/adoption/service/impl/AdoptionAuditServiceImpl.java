package com.wanderingpet.module.adoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.enums.ApplicationStatus;
import com.wanderingpet.common.enums.PetStatus;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.SeqUtil;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.dto.AuditActionDTO;
import com.wanderingpet.module.adoption.dto.HomeVisitDTO;
import com.wanderingpet.module.adoption.dto.InterviewDTO;
import com.wanderingpet.module.adoption.dto.ReturnDTO;
import com.wanderingpet.module.adoption.dto.SignDTO;
import com.wanderingpet.module.adoption.entity.AdoptionApplication;
import com.wanderingpet.module.adoption.entity.AdoptionRecord;
import com.wanderingpet.module.adoption.mapper.AdoptionApplicationMapper;
import com.wanderingpet.module.adoption.mapper.AdoptionRecordMapper;
import com.wanderingpet.module.adoption.service.AdoptionAuditService;
import com.wanderingpet.module.adoption.vo.AdoptionRecordVO;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;
import com.wanderingpet.module.followup.entity.FollowUp;
import com.wanderingpet.module.followup.mapper.FollowUpMapper;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 领养审核服务实现（机构视角）
 *
 * <p>每次流转：状态校验 → 更新申请 → 落流水 → 通知申请人；数据权限限定本机构。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdoptionAuditServiceImpl implements AdoptionAuditService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AdoptionApplicationMapper applicationMapper;
    private final AdoptionRecordMapper recordMapper;
    private final FollowUpMapper followUpMapper;
    private final PetMapper petMapper;
    private final ShelterMapper shelterMapper;
    private final NotificationService notificationService;
    private final SeqUtil seqUtil;
    private final AdoptionServiceImpl adoptionService;

    // ---------------------------------------------------------------- 查询

    @Override
    public PageResult<ApplicationListVO> pageShelter(ApplicationQueryDTO query) {
        LoginUser user = requireShelter();
        query.setShelterId(user.getShelterId());
        Page<ApplicationListVO> page = applicationMapper.selectPageWithNames(
                new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)), query);
        return PageResult.of(page);
    }

    @Override
    public PageResult<ApplicationListVO> pageAdmin(ApplicationQueryDTO query) {
        Page<ApplicationListVO> page = applicationMapper.selectPageWithNames(
                new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)), query);
        return PageResult.of(page);
    }

    // ---------------------------------------------------------------- 状态流转

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(Long id) {
        LoginUser user = requireShelter();
        AdoptionApplication application = requireOwnedApplication(id, user);
        transition(application, ApplicationStatus.SUBMITTED, ApplicationStatus.REVIEWING,
                user.getUserId(), "救助站受理，开始资料审核", null, null);
        application.setAuditorId(user.getUserId());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void interview(Long id, InterviewDTO dto) {
        LoginUser user = requireShelter();
        AdoptionApplication application = requireOwnedApplication(id, user);
        if (dto.getInterviewTime() == null || dto.getInterviewTime().isBlank()) {
            throw BizException.of(ResultCode.BAD_REQUEST, "请选择面谈时间");
        }
        LocalDateTime time = LocalDateTime.parse(dto.getInterviewTime(), DATE_TIME);
        transition(application, ApplicationStatus.REVIEWING, ApplicationStatus.INTERVIEW,
                user.getUserId(), "初筛通过，面谈时间 " + dto.getInterviewTime(), dto.getRemark(), time);
        application.setInterviewTime(time);
        application.setAuditorId(user.getUserId());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void homeVisit(Long id, HomeVisitDTO dto) {
        LoginUser user = requireShelter();
        AdoptionApplication application = requireOwnedApplication(id, user);
        LocalDateTime time = dto.getHomeVisitTime() == null || dto.getHomeVisitTime().isBlank()
                ? null : LocalDateTime.parse(dto.getHomeVisitTime(), DATE_TIME);
        transition(application, ApplicationStatus.INTERVIEW, ApplicationStatus.HOME_VISIT,
                user.getUserId(), "面谈通过，家访时间 " + (dto.getHomeVisitTime() == null ? "待定" : dto.getHomeVisitTime()),
                dto.getRemark(), null);
        application.setHomeVisitTime(time);
        applicationMapper.updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, AuditActionDTO dto) {
        LoginUser user = requireShelter();
        AdoptionApplication application = requireOwnedApplication(id, user);
        transition(application, ApplicationStatus.HOME_VISIT, ApplicationStatus.APPROVED,
                user.getUserId(), dto.getRemark() == null ? "家访通过，审核完成" : dto.getRemark(),
                dto.getRemark(), null);
        application.setAuditRemark(dto.getRemark() == null ? "审核通过" : dto.getRemark());
        application.setAuditorId(user.getUserId());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, AuditActionDTO dto) {
        LoginUser user = requireShelter();
        AdoptionApplication application = requireOwnedApplication(id, user);
        ApplicationStatus from = ApplicationStatus.valueOf(application.getStatus());
        if (!from.canReject()) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR);
        }
        String remark = dto.getRemark() == null || dto.getRemark().isBlank() ? "未通过审核" : dto.getRemark();
        transition(application, from, ApplicationStatus.REJECTED, user.getUserId(), remark, null, null);
        application.setStatus(ApplicationStatus.REJECTED.name());
        application.setAuditRemark(remark);
        application.setAuditorId(user.getUserId());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        adoptionService.releasePetIfIdle(application.getPetId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sign(Long id, SignDTO dto) {
        LoginUser user = requireShelter();
        AdoptionApplication application = requireOwnedApplication(id, user);
        if (!ApplicationStatus.APPROVED.name().equals(application.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "仅审核通过的申请可以签约");
        }
        transition(application, ApplicationStatus.APPROVED, ApplicationStatus.SIGNED,
                user.getUserId(), dto.getSignDate() == null ? "已签署领养协议" : "签署领养协议（" + dto.getSignDate() + "）", null, null);
        application.setStatus(ApplicationStatus.SIGNED.name());
        application.setAuditorId(user.getUserId());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);

        // 生成领养记录
        AdoptionRecord record = new AdoptionRecord();
        record.setAdoptionNo(seqUtil.next(BizConstant.SEQ_ADOPTION, "ADO"));
        record.setApplicationId(id);
        record.setPetId(application.getPetId());
        record.setUserId(application.getUserId());
        record.setShelterId(application.getShelterId());
        record.setSignDate(dto.getSignDate() == null || dto.getSignDate().isBlank()
                ? LocalDate.now() : LocalDate.parse(dto.getSignDate()));
        record.setAgreementObjectName(dto.getAgreementObjectName() == null ? "" : dto.getAgreementObjectName());
        record.setStatus("ADOPTED");
        recordMapper.insert(record);
        adoptionService.saveLog(id, ApplicationStatus.APPROVED.name(), ApplicationStatus.SIGNED.name(),
                user.getUserId(), "SHELTER", "生成领养记录 " + record.getAdoptionNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        LoginUser user = requireShelter();
        AdoptionApplication application = requireOwnedApplication(id, user);
        if (!ApplicationStatus.SIGNED.name().equals(application.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "仅已签约的申请可以完成领养");
        }
        transition(application, ApplicationStatus.SIGNED, ApplicationStatus.ADOPTED,
                user.getUserId(), "领养完成，进入回访计划", null, null);
        application.setStatus(ApplicationStatus.ADOPTED.name());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);

        // 宠物 → ADOPTED
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, application.getPetId())
                .set(Pet::getStatus, PetStatus.ADOPTED.name()));

        // 生成 4 次回访任务（第 7/30/90/180 天）
        AdoptionRecord record = recordMapper.selectOne(new LambdaQueryWrapper<AdoptionRecord>()
                .eq(AdoptionRecord::getApplicationId, id).last("LIMIT 1"));
        if (record != null) {
            LocalDate base = record.getSignDate() == null ? LocalDate.now() : record.getSignDate();
            for (int i = 0; i < BizConstant.FOLLOW_UP_DAYS.length; i++) {
                FollowUp followUp = new FollowUp();
                followUp.setRecordId(record.getId());
                followUp.setApplicationId(id);
                followUp.setRound(i + 1);
                followUp.setPlanDate(base.plusDays(BizConstant.FOLLOW_UP_DAYS[i]));
                followUp.setHealthStatus("");
                followUp.setVaccineStatus("");
                followUp.setLifeDesc("");
                followUp.setStatus("PENDING");
                followUpMapper.insert(followUp);
            }
        }
        notificationService.send(application.getUserId(), AuthConstant.USER_TYPE_USER,
                "领养完成", "申请 %s 已完成领养。我们已为 %s 安排 4 次定期回访（第 7/30/90/180 天），请在回访窗口填写生活近况。"
                        .formatted(application.getApplyNo(), petName(application.getPetId())),
                BizConstant.BIZ_APPLICATION, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnAdoption(Long recordId, ReturnDTO dto) {
        LoginUser user = requireShelter();
        AdoptionRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        if (!Objects.equals(record.getShelterId(), user.getShelterId())) {
            throw BizException.of(ResultCode.FORBIDDEN, "只能操作本机构的领养记录");
        }
        if (!"ADOPTED".equals(record.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "仅领养完成的记录可以退养");
        }
        record.setStatus("RETURNED");
        record.setReturnReason(dto.getReason());
        record.setReturnTime(LocalDateTime.now());
        recordMapper.updateById(record);

        // 宠物回到可领养，申请单留痕
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, record.getPetId())
                .set(Pet::getStatus, PetStatus.AVAILABLE.name()));
        AdoptionApplication application = applicationMapper.selectById(record.getApplicationId());
        if (application != null) {
            transition(application, ApplicationStatus.ADOPTED, ApplicationStatus.RETURNED,
                    user.getUserId(), "退养：" + dto.getReason(), null, null);
            application.setStatus(ApplicationStatus.RETURNED.name());
            applicationMapper.updateById(application);
        }
        notificationService.send(record.getUserId(), AuthConstant.USER_TYPE_USER,
                "退养已登记", "领养记录 %s 已登记退养，宠物回到待领养状态。感谢您及时反馈。".formatted(record.getAdoptionNo()),
                BizConstant.BIZ_APPLICATION, record.getApplicationId());
    }

    @Override
    public PageResult<AdoptionRecordVO> pageRecords(Long shelterId, Integer pageNum, Integer pageSize, String status) {
        LambdaQueryWrapper<AdoptionRecord> wrapper = new LambdaQueryWrapper<AdoptionRecord>()
                .eq(shelterId != null, AdoptionRecord::getShelterId, shelterId)
                .eq(status != null && !status.isBlank(), AdoptionRecord::getStatus, status)
                .orderByDesc(AdoptionRecord::getCreateTime);
        Page<AdoptionRecord> page = recordMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)), wrapper);
        List<AdoptionRecordVO> list = page.getRecords().stream().map(record -> {
            AdoptionRecordVO vo = new AdoptionRecordVO();
            BeanUtils.copyProperties(record, vo);
            vo.setSignDate(record.getSignDate() == null ? null : record.getSignDate().toString());
            vo.setReturnTime(record.getReturnTime() == null ? null : record.getReturnTime().toString().replace('T', ' '));
            vo.setCreateTime(record.getCreateTime() == null ? null : record.getCreateTime().toString().replace('T', ' '));
            Pet pet = petMapper.selectById(record.getPetId());
            if (pet != null) {
                vo.setPetName(pet.getName());
                vo.setPetCover(pet.getCoverImage());
            }
            Shelter shelter = shelterMapper.selectById(record.getShelterId());
            vo.setShelterName(shelter == null ? null : shelter.getName());
            vo.setFollowUps(List.of());
            return vo;
        }).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    // ---------------------------------------------------------------- private

    private void transition(AdoptionApplication application, ApplicationStatus expectFrom,
                            ApplicationStatus to, Long operatorId, String remark, String extra, LocalDateTime interviewTime) {
        if (!application.getStatus().equals(expectFrom.name())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR,
                    "当前状态（%s）不允许该操作".formatted(application.getStatus()));
        }
        application.setStatus(to.name());
        if (interviewTime != null) {
            application.setInterviewTime(interviewTime);
        }
        applicationMapper.updateById(application);
        adoptionService.saveLog(application.getId(), expectFrom.name(), to.name(), operatorId, "SHELTER", remark);
        notificationService.send(application.getUserId(), AuthConstant.USER_TYPE_USER,
                statusTitle(to), statusContent(application, to, remark),
                BizConstant.BIZ_APPLICATION, application.getId());
    }

    private String statusTitle(ApplicationStatus to) {
        return switch (to) {
            case REVIEWING -> "申请已受理";
            case INTERVIEW -> "通过初筛，请按时面谈";
            case HOME_VISIT -> "面谈通过，将安排家访";
            case APPROVED -> "领养审核已通过";
            case SIGNED -> "领养协议已签署";
            case ADOPTED -> "领养完成";
            case REJECTED -> "领养申请未通过";
            case CANCELED -> "领养申请已撤回";
            case EXPIRED -> "领养申请已超期关闭";
            case RETURNED -> "退养已登记";
            default -> "申请状态更新";
        };
    }

    private String statusContent(AdoptionApplication application, ApplicationStatus to, String remark) {
        String pet = petName(application.getPetId());
        return switch (to) {
            case REVIEWING -> "您的申请 %s（宠物：%s）已由救助站受理，请耐心等待审核结果。".formatted(application.getApplyNo(), pet);
            case INTERVIEW -> "您的申请 %s 已通过初筛，面谈时间：%s。请提前 10 分钟到场。".formatted(application.getApplyNo(), application.getInterviewTime());
            case HOME_VISIT -> "您的申请 %s 面谈通过，家访时间：%s。".formatted(application.getApplyNo(), application.getHomeVisitTime());
            case APPROVED -> "您的申请 %s 审核通过，请按机构通知到站办理签约手续。".formatted(application.getApplyNo());
            case SIGNED -> "领养协议已签署，请等待机构确认完成领养。";
            case REJECTED -> "您的申请 %s 未通过：%s。您可以继续浏览其他待领养宠物。".formatted(application.getApplyNo(), remark);
            case EXPIRED -> "您的申请 %s 超过 7 天未推进，已自动关闭。".formatted(application.getApplyNo());
            default -> "申请 %s 状态已更新为「%s」。".formatted(application.getApplyNo(), to.name());
        };
    }

    private String petName(Long petId) {
        Pet pet = petMapper.selectById(petId);
        return pet == null ? "" : pet.getName();
    }

    private LoginUser requireShelter() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isShelter() || user.getShelterId() == null) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return user;
    }

    private AdoptionApplication requireOwnedApplication(Long id, LoginUser user) {
        AdoptionApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw BizException.of(ResultCode.APPLICATION_NOT_FOUND);
        }
        if (!Objects.equals(application.getShelterId(), user.getShelterId())) {
            throw BizException.of(ResultCode.FORBIDDEN, "只能操作本机构的申请");
        }
        return application;
    }
}
