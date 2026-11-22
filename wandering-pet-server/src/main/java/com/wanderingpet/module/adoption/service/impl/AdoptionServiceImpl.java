package com.wanderingpet.module.adoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.document.UserBehavior;
import com.wanderingpet.common.enums.ApplicationStatus;
import com.wanderingpet.common.enums.PetStatus;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.MaskUtil;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.common.util.SeqUtil;
import com.wanderingpet.module.adoption.dto.ApplicationQueryDTO;
import com.wanderingpet.module.adoption.dto.ApplicationSubmitDTO;
import com.wanderingpet.module.adoption.entity.AdoptionApplication;
import com.wanderingpet.module.adoption.entity.AdoptionFlowLog;
import com.wanderingpet.module.adoption.entity.AdoptionRecord;
import com.wanderingpet.module.adoption.mapper.AdoptionApplicationMapper;
import com.wanderingpet.module.adoption.mapper.AdoptionFlowLogMapper;
import com.wanderingpet.module.adoption.mapper.AdoptionRecordMapper;
import com.wanderingpet.module.adoption.service.AdoptionService;
import com.wanderingpet.module.adoption.vo.AdoptionRecordVO;
import com.wanderingpet.module.adoption.vo.ApplicationDetailVO;
import com.wanderingpet.module.adoption.vo.ApplicationListVO;
import com.wanderingpet.module.adoption.vo.FlowLogVO;
import com.wanderingpet.module.followup.entity.FollowUp;
import com.wanderingpet.module.followup.mapper.FollowUpMapper;
import com.wanderingpet.module.chat.service.ChatService;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 领养申请服务实现（申请人视角）
 *
 * <p>防重复申请双保险：数据库 uk_pet_user_active 唯一索引 + Redis SETNX 锁。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionApplicationMapper applicationMapper;
    private final AdoptionFlowLogMapper flowLogMapper;
    private final AdoptionRecordMapper recordMapper;
    private final FollowUpMapper followUpMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final RedisUtil redisUtil;
    private final SeqUtil seqUtil;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ApplicationSubmitDTO dto) {
        LoginUser user = requireUser();
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null) {
            throw BizException.of(ResultCode.PET_NOT_FOUND);
        }
        if (!PetStatus.AVAILABLE.name().equals(pet.getStatus())) {
            throw BizException.of(ResultCode.PET_NOT_AVAILABLE);
        }
        // 防并发重复申请锁（10 秒）
        String lockKey = RedisKeyConstant.LOCK_ADOPTION.formatted(pet.getId(), user.getUserId());
        if (!redisUtil.tryLock(lockKey, "1", Duration.ofSeconds(10))) {
            throw BizException.of(ResultCode.APPLICATION_EXISTS);
        }
        try {
            // 业务校验：非终态申请唯一
            Long active = applicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                    .eq(AdoptionApplication::getPetId, pet.getId())
                    .eq(AdoptionApplication::getUserId, user.getUserId())
                    .in(AdoptionApplication::getStatus, List.of(
                            "DRAFT", "SUBMITTED", "REVIEWING", "INTERVIEW", "HOME_VISIT", "APPROVED", "SIGNED")));
            if (active != null && active > 0) {
                throw BizException.of(ResultCode.APPLICATION_EXISTS);
            }
            AdoptionApplication application = new AdoptionApplication();
            BeanUtils.copyProperties(dto, application);
            application.setApplyNo(seqUtil.next(BizConstant.SEQ_APPLY, "APP"));
            application.setUserId(user.getUserId());
            application.setShelterId(pet.getShelterId());
            application.setStatus(ApplicationStatus.SUBMITTED.name());
            application.setAuditRemark("");
            applicationMapper.insert(application);
            saveLog(application.getId(), "", ApplicationStatus.SUBMITTED.name(),
                    user.getUserId(), "USER", "提交领养申请");

            // 宠物锁定：AVAILABLE → PENDING
            petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                    .eq(Pet::getId, pet.getId())
                    .eq(Pet::getStatus, PetStatus.AVAILABLE.name())
                    .set(Pet::getStatus, PetStatus.PENDING.name()));

            // 通知机构
            notificationService.send(pet.getShelterId(), AuthConstant.USER_TYPE_SHELTER,
                    "收到新的领养申请", "您有一份新的领养申请（%s，宠物：%s）待受理，请及时处理。"
                            .formatted(application.getApplyNo(), pet.getName()),
                    BizConstant.BIZ_APPLICATION, application.getId());
            // 领养资料自动补全：把本次申请的通用信息回写到用户档案，再次申请时免重复填写
            User profile = userMapper.selectById(user.getUserId());
            if (profile != null) {
                profile.setAdopterName(dto.getApplicantName());
                profile.setAdopterAge(dto.getApplicantAge());
                if (dto.getOccupation() != null && !dto.getOccupation().isBlank()) {
                    profile.setOccupation(dto.getOccupation());
                }
                profile.setHousingType(dto.getHousingType());
                profile.setHasPetExperience(dto.getHasPetExperience());
                profile.setFamilyAgree(dto.getFamilyAgree());
                profile.setHasChildren(dto.getHasChildren());
                profile.setHasAllergy(dto.getHasAllergy());
                if (dto.getCity() != null && !dto.getCity().isBlank()) {
                    profile.setCity(dto.getCity());
                }
                userMapper.updateById(profile);
            }
            // Boss直聘式自动建联：申请提交后自动创建与发布机构的会话，
            // 并发送「申请卡片」消息（WS 实时推送），发布人打开聊天即可快速查看申请人信息
            chatService.sendApplicationCard(user.getUserId(), pet.getShelterId(), application.getId());
            // 行为埋点
            mongoTemplate.save(UserBehavior.builder()
                    .userId(user.getUserId()).behaviorType("APPLY")
                    .targetType("PET").targetId(pet.getId())
                    .createTime(LocalDateTime.now()).build());
            return application.getId();
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        LoginUser user = requireUser();
        AdoptionApplication application = requireOwnedApplication(id, user.getUserId());
        ApplicationStatus status = ApplicationStatus.valueOf(application.getStatus());
        if (!status.canCancel()) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "当前状态不可撤回");
        }
        application.setStatus(ApplicationStatus.CANCELED.name());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        saveLog(id, status.name(), ApplicationStatus.CANCELED.name(), user.getUserId(), "USER", "申请人主动撤回");
        // 撤回即解除本申请对宠物的锁定：PENDING → AVAILABLE，让宠物回到可领养列表。
        // 不走 releasePetIfIdle：其"无其他非终态申请才释放"的守卫会把宠物卡死在 PENDING
        // （残留的历史/演示申请永远不推进也不超期），导致撤回后宠物无法回到可领养栏。
        releasePet(application.getPetId());
        notificationService.send(application.getShelterId(), AuthConstant.USER_TYPE_SHELTER,
                "领养申请已撤回", "申请 %s（%s）已被申请人撤回。".formatted(application.getApplyNo(), petName(application.getPetId())),
                BizConstant.BIZ_APPLICATION, id);
    }

    @Override
    public PageResult<ApplicationListVO> pageMy(ApplicationQueryDTO query) {
        LoginUser user = requireUser();
        query.setShelterId(null);
        query.setUserId(user.getUserId());
        Page<ApplicationListVO> page = applicationMapper.selectMyPage(
                new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)), query);
        return PageResult.of(page);
    }

    @Override
    public ApplicationDetailVO detail(Long id) {
        LoginUser user = UserContext.current();
        if (user == null) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        AdoptionApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw BizException.of(ResultCode.APPLICATION_NOT_FOUND);
        }
        boolean owner = user.isUser() && Objects.equals(application.getUserId(), user.getUserId());
        boolean shelter = user.isShelter() && Objects.equals(application.getShelterId(), user.getShelterId());
        if (!owner && !shelter && !user.isAdmin()) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        ApplicationDetailVO vo = new ApplicationDetailVO();
        BeanUtils.copyProperties(application, vo);
        vo.setApplicantPhone(MaskUtil.maskPhone(application.getApplicantPhone()));
        vo.setInterviewTime(application.getInterviewTime() == null ? null : application.getInterviewTime().toString().replace('T', ' '));
        vo.setHomeVisitTime(application.getHomeVisitTime() == null ? null : application.getHomeVisitTime().toString().replace('T', ' '));
        vo.setAuditTime(application.getAuditTime() == null ? null : application.getAuditTime().toString().replace('T', ' '));
        vo.setCreateTime(application.getCreateTime() == null ? null : application.getCreateTime().toString().replace('T', ' '));
        // 宠物摘要
        Pet pet = petMapper.selectById(application.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
            vo.setPetCover(pet.getCoverImage());
        }
        // 申请人昵称
        User applicant = userMapper.selectById(application.getUserId());
        vo.setUserNickname(applicant == null ? null : applicant.getNickname());
        vo.setFlowLogs(flowLogs(id));
        return vo;
    }

    @Override
    public List<FlowLogVO> flowLogs(Long id) {
        return flowLogMapper.selectList(new LambdaQueryWrapper<AdoptionFlowLog>()
                        .eq(AdoptionFlowLog::getApplicationId, id)
                        .orderByAsc(AdoptionFlowLog::getCreateTime)).stream()
                .map(this::toFlowLogVO).toList();
    }

    @Override
    public List<AdoptionRecordVO> myRecords() {
        LoginUser user = requireUser();
        List<AdoptionRecord> records = recordMapper.selectList(new LambdaQueryWrapper<AdoptionRecord>()
                .eq(AdoptionRecord::getUserId, user.getUserId())
                .orderByDesc(AdoptionRecord::getCreateTime));
        return records.stream().map(record -> buildRecordVO(record, false)).toList();
    }

    @Override
    public AdoptionRecordVO recordDetail(Long recordId) {
        LoginUser user = UserContext.current();
        if (user == null) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        AdoptionRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        boolean owner = user.isUser() && Objects.equals(record.getUserId(), user.getUserId());
        boolean shelter = user.isShelter() && Objects.equals(record.getShelterId(), user.getShelterId());
        if (!owner && !shelter && !user.isAdmin()) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return buildRecordVO(record, user.isShelter());
    }

    // ---------------------------------------------------------------- 供审核服务复用

    public void saveLog(Long applicationId, String from, String to, Long operatorId, String operatorType, String remark) {
        AdoptionFlowLog flowLog = new AdoptionFlowLog();
        flowLog.setApplicationId(applicationId);
        flowLog.setFromStatus(from == null ? "" : from);
        flowLog.setToStatus(to);
        flowLog.setOperatorId(operatorId);
        flowLog.setOperatorType(operatorType);
        flowLog.setRemark(remark == null ? "" : remark);
        flowLogMapper.insert(flowLog);
    }

    /** 释放宠物：无其他非终态申请时 PENDING → AVAILABLE */
    public void releasePetIfIdle(Long petId) {
        Long active = applicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getPetId, petId)
                .in(AdoptionApplication::getStatus, List.of(
                        "SUBMITTED", "REVIEWING", "INTERVIEW", "HOME_VISIT", "APPROVED", "SIGNED")));
        if (active == null || active == 0) {
            releasePet(petId);
        }
    }

    /** 直接释放宠物锁定：PENDING → AVAILABLE（带 PENDING 条件，不影响已领养/下架等其他状态） */
    public void releasePet(Long petId) {
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, petId)
                .eq(Pet::getStatus, PetStatus.PENDING.name())
                .set(Pet::getStatus, PetStatus.AVAILABLE.name()));
    }

    AdoptionApplication requireOwnedApplication(Long id, Long userId) {
        AdoptionApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw BizException.of(ResultCode.APPLICATION_NOT_FOUND);
        }
        if (!Objects.equals(application.getUserId(), userId)) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return application;
    }

    String petName(Long petId) {
        Pet pet = petMapper.selectById(petId);
        return pet == null ? "" : pet.getName();
    }

    FlowLogVO toFlowLogVO(AdoptionFlowLog flowLog) {
        FlowLogVO vo = new FlowLogVO();
        BeanUtils.copyProperties(flowLog, vo);
        vo.setCreateTime(flowLog.getCreateTime() == null ? null : flowLog.getCreateTime().toString().replace('T', ' '));
        return vo;
    }

    AdoptionRecordVO buildRecordVO(AdoptionRecord record, boolean shelterView) {
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
        User adopter = userMapper.selectById(record.getUserId());
        vo.setUserNickname(adopter == null ? null : adopter.getNickname());
        List<FollowUp> followUps = followUpMapper.selectList(new LambdaQueryWrapper<FollowUp>()
                .eq(FollowUp::getRecordId, record.getId())
                .orderByAsc(FollowUp::getRound));
        vo.setFollowUps(followUps.stream().map(f -> {
            com.wanderingpet.module.followup.vo.FollowUpVO fvo = new com.wanderingpet.module.followup.vo.FollowUpVO();
            BeanUtils.copyProperties(f, fvo);
            fvo.setPlanDate(f.getPlanDate() == null ? null : f.getPlanDate().toString());
            fvo.setActualDate(f.getActualDate() == null ? null : f.getActualDate().toString());
            fvo.setImages(parseImages(f.getImages()));
            return fvo;
        }).toList());
        return vo;
    }

    List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return List.of();
        }
        try {
            return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                    .readValue(imagesJson, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() { });
        } catch (Exception e) {
            return List.of();
        }
    }

    private LoginUser requireUser() {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return user;
    }
}
