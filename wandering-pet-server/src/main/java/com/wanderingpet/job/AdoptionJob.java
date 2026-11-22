package com.wanderingpet.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.enums.ApplicationStatus;
import com.wanderingpet.common.enums.FollowUpStatus;
import com.wanderingpet.common.enums.PetStatus;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.module.adoption.entity.AdoptionApplication;
import com.wanderingpet.module.adoption.entity.AdoptionRecord;
import com.wanderingpet.module.adoption.mapper.AdoptionApplicationMapper;
import com.wanderingpet.module.adoption.mapper.AdoptionRecordMapper;
import com.wanderingpet.module.adoption.service.impl.AdoptionServiceImpl;
import com.wanderingpet.module.followup.entity.FollowUp;
import com.wanderingpet.module.followup.mapper.FollowUpMapper;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：申请超期自动关闭 + 回访逾期标记与提醒（Redis 分布式锁防多实例重复执行）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdoptionJob {

    private final RedisUtil redisUtil;
    private final AdoptionApplicationMapper applicationMapper;
    private final AdoptionRecordMapper recordMapper;
    private final FollowUpMapper followUpMapper;
    private final PetMapper petMapper;
    private final NotificationService notificationService;
    private final AdoptionServiceImpl adoptionService;

    /**
     * 每 30 分钟：SUBMITTED/REVIEWING 超过 7 天未推进 → EXPIRED，通知双方并释放宠物
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void expireApplications() {
        if (!tryLock("expire-applications")) {
            return;
        }
        try {
            LocalDateTime deadline = LocalDateTime.now().minusDays(BizConstant.APPLICATION_EXPIRE_DAYS);
            List<AdoptionApplication> expired = applicationMapper.selectList(new LambdaQueryWrapper<AdoptionApplication>()
                    .in(AdoptionApplication::getStatus, ApplicationStatus.SUBMITTED.name(), ApplicationStatus.REVIEWING.name())
                    .lt(AdoptionApplication::getCreateTime, deadline));
            for (AdoptionApplication application : expired) {
                String fromStatus = application.getStatus();
                application.setStatus(ApplicationStatus.EXPIRED.name());
                application.setAuditTime(LocalDateTime.now());
                applicationMapper.updateById(application);
                adoptionService.saveLog(application.getId(), fromStatus,
                        ApplicationStatus.EXPIRED.name(), 0L, "SYSTEM", "超过 7 天未推进，自动关闭");
                adoptionService.releasePetIfIdle(application.getPetId());
                notificationService.send(application.getUserId(), AuthConstant.USER_TYPE_USER,
                        "领养申请已超期关闭", "您的申请 %s 超过 7 天未推进，已自动关闭。您可以继续浏览其他待领养宠物。"
                                .formatted(application.getApplyNo()), BizConstant.BIZ_APPLICATION, application.getId());
                notificationService.send(application.getShelterId(), AuthConstant.USER_TYPE_SHELTER,
                        "领养申请已超期关闭", "申请 %s（%s）超过 7 天未推进，已自动关闭。"
                                .formatted(application.getApplyNo(), petName(application.getPetId())),
                        BizConstant.BIZ_APPLICATION, application.getId());
            }
            if (!expired.isEmpty()) {
                log.info("超期申请自动关闭 {} 条", expired.size());
            }
        } finally {
            unlock("expire-applications");
        }
    }

    /**
     * 每天 09:05：回访逾期标记（超过计划日期 3 天未完成 → OVERDUE）+ 即将到期提醒（3 天内）
     */
    @Scheduled(cron = "0 5 9 * * ?")
    public void followUpRemind() {
        if (!tryLock("follow-up-remind")) {
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            // 逾期标记
            List<FollowUp> overdue = followUpMapper.selectList(new LambdaQueryWrapper<FollowUp>()
                    .eq(FollowUp::getStatus, FollowUpStatus.PENDING.name())
                    .lt(FollowUp::getPlanDate, today.minusDays(3)));
            for (FollowUp followUp : overdue) {
                followUpMapper.update(null, new LambdaUpdateWrapper<FollowUp>()
                        .eq(FollowUp::getId, followUp.getId())
                        .set(FollowUp::getStatus, FollowUpStatus.OVERDUE.name()));
                notifyFollowUp(followUp, "回访已逾期", overdueContent(followUp));
            }
            // 3 天内到期提醒
            List<FollowUp> upcoming = followUpMapper.selectList(new LambdaQueryWrapper<FollowUp>()
                    .eq(FollowUp::getStatus, FollowUpStatus.PENDING.name())
                    .between(FollowUp::getPlanDate, today, today.plusDays(3)));
            for (FollowUp followUp : upcoming) {
                notifyFollowUp(followUp, "回访提醒", "第 %d 次回访计划日期为 %s，请及时填写回访表。"
                        .formatted(followUp.getRound(), followUp.getPlanDate()));
            }
            log.info("回访任务处理完成：逾期 {} 条，即将到期 {} 条", overdue.size(), upcoming.size());
        } finally {
            unlock("follow-up-remind");
        }
    }

    private void notifyFollowUp(FollowUp followUp, String title, String content) {
        AdoptionRecord record = recordMapper.selectById(followUp.getRecordId());
        if (record == null) {
            return;
        }
        notificationService.send(record.getUserId(), AuthConstant.USER_TYPE_USER, title, content,
                BizConstant.BIZ_FOLLOW_UP, followUp.getId());
        notificationService.send(record.getShelterId(), AuthConstant.USER_TYPE_SHELTER, title,
                "领养记录 %s 的第 %d 次回访：%s".formatted(record.getAdoptionNo(), followUp.getRound(), content),
                BizConstant.BIZ_FOLLOW_UP, followUp.getId());
    }

    private String overdueContent(FollowUp followUp) {
        return "第 %d 次回访（计划日期 %s）已逾期，请尽快填写回访表。".formatted(followUp.getRound(), followUp.getPlanDate());
    }

    private String petName(Long petId) {
        Pet pet = petMapper.selectById(petId);
        return pet == null ? "" : pet.getName();
    }

    private boolean tryLock(String job) {
        return redisUtil.tryLock(RedisKeyConstant.LOCK_JOB.formatted(job), "1", Duration.ofMinutes(10));
    }

    private void unlock(String job) {
        redisUtil.unlock(RedisKeyConstant.LOCK_JOB.formatted(job));
    }
}
