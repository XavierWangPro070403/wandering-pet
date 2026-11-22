package com.wanderingpet.module.followup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.enums.FollowUpStatus;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.adoption.entity.AdoptionRecord;
import com.wanderingpet.module.adoption.mapper.AdoptionRecordMapper;
import com.wanderingpet.module.followup.dto.FollowUpSubmitDTO;
import com.wanderingpet.module.followup.entity.FollowUp;
import com.wanderingpet.module.followup.mapper.FollowUpMapper;
import com.wanderingpet.module.followup.service.FollowUpService;
import com.wanderingpet.module.followup.vo.FollowUpVO;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 回访服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpMapper followUpMapper;
    private final AdoptionRecordMapper recordMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id, FollowUpSubmitDTO dto) {
        LoginUser user = requireUser();
        FollowUp followUp = requireOwnedFollowUp(id, user.getUserId());
        if (FollowUpStatus.DONE.name().equals(followUp.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "该回访已完成，无需重复填写");
        }
        followUp.setWeight(dto.getWeight());
        followUp.setHealthStatus(dto.getHealthStatus() == null ? "" : dto.getHealthStatus());
        followUp.setVaccineStatus(dto.getVaccineStatus() == null ? "" : dto.getVaccineStatus());
        followUp.setLifeDesc(dto.getLifeDesc() == null ? "" : dto.getLifeDesc());
        followUp.setActualDate(LocalDate.now());
        followUp.setStatus(FollowUpStatus.DONE.name());
        followUp.setImages(toJson(dto.getImages()));
        followUpMapper.updateById(followUp);

        // 通知机构归档确认
        AdoptionRecord record = recordMapper.selectById(followUp.getRecordId());
        if (record != null) {
            notificationService.send(record.getShelterId(), AuthConstant.USER_TYPE_SHELTER,
                    "回访表已填写", "第 %d 次回访（计划日期 %s）已由领养人填写，请查看并归档。"
                            .formatted(followUp.getRound(), followUp.getPlanDate()),
                    BizConstant.BIZ_FOLLOW_UP, followUp.getId());
        }
    }

    @Override
    public PageResult<FollowUpVO> pageMy(Integer pageNum, Integer pageSize, String status) {
        LoginUser user = requireUser();
        // 通过本人领养记录过滤
        List<Long> recordIds = recordMapper.selectList(new LambdaQueryWrapper<AdoptionRecord>()
                        .eq(AdoptionRecord::getUserId, user.getUserId()))
                .stream().map(AdoptionRecord::getId).toList();
        if (recordIds.isEmpty()) {
            return new PageResult<>(List.of(), 0, pageNum, pageSize, 0);
        }
        Page<FollowUp> page = followUpMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<FollowUp>()
                        .in(FollowUp::getRecordId, recordIds)
                        .eq(status != null && !status.isBlank(), FollowUp::getStatus, status)
                        .orderByAsc(FollowUp::getPlanDate));
        List<FollowUpVO> list = page.getRecords().stream().map(f -> toVO(f, true)).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public PageResult<FollowUpVO> pageShelter(Integer pageNum, Integer pageSize, String status, Integer round) {
        LoginUser user = requireShelter();
        List<Long> recordIds = recordMapper.selectList(new LambdaQueryWrapper<AdoptionRecord>()
                        .eq(AdoptionRecord::getShelterId, user.getShelterId()))
                .stream().map(AdoptionRecord::getId).toList();
        if (recordIds.isEmpty()) {
            return new PageResult<>(List.of(), 0, pageNum, pageSize, 0);
        }
        Page<FollowUp> page = followUpMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<FollowUp>()
                        .in(FollowUp::getRecordId, recordIds)
                        .eq(status != null && !status.isBlank(), FollowUp::getStatus, status)
                        .eq(round != null, FollowUp::getRound, round)
                        .orderByAsc(FollowUp::getPlanDate));
        List<FollowUpVO> list = page.getRecords().stream().map(f -> toVO(f, false)).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public void remind(Long id) {
        LoginUser user = requireShelter();
        FollowUp followUp = followUpMapper.selectById(id);
        requireOwnedTask(followUp, user.getShelterId());
        AdoptionRecord record = recordMapper.selectById(followUp.getRecordId());
        if (record == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        notificationService.send(record.getUserId(), AuthConstant.USER_TYPE_USER,
                "回访提醒", "第 %d 次回访计划日期为 %s，请前往「我的回访」填写 %s 的生活近况（体重、疫苗、生活照）。"
                        .formatted(followUp.getRound(), followUp.getPlanDate(), petName(record.getPetId())),
                BizConstant.BIZ_FOLLOW_UP, followUp.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long id) {
        LoginUser user = requireShelter();
        FollowUp followUp = followUpMapper.selectById(id);
        requireOwnedTask(followUp, user.getShelterId());
        if (!FollowUpStatus.DONE.name().equals(followUp.getStatus())) {
            throw BizException.of(ResultCode.APPLICATION_STATUS_ERROR, "回访表尚未填写，无法归档");
        }
        followUp.setHandlerId(user.getUserId());
        followUpMapper.updateById(followUp);
    }

    // ---------------------------------------------------------------- private

    private void requireOwnedTask(FollowUp followUp, Long shelterId) {
        if (followUp == null) {
            throw BizException.of(ResultCode.FOLLOW_UP_NOT_FOUND);
        }
        AdoptionRecord record = recordMapper.selectById(followUp.getRecordId());
        if (record == null || !Objects.equals(record.getShelterId(), shelterId)) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
    }

    private FollowUp requireOwnedFollowUp(Long id, Long userId) {
        FollowUp followUp = followUpMapper.selectById(id);
        if (followUp == null) {
            throw BizException.of(ResultCode.FOLLOW_UP_NOT_FOUND);
        }
        AdoptionRecord record = recordMapper.selectById(followUp.getRecordId());
        if (record == null || !Objects.equals(record.getUserId(), userId)) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return followUp;
    }

    private FollowUpVO toVO(FollowUp followUp, boolean ownerView) {
        FollowUpVO vo = new FollowUpVO();
        BeanUtils.copyProperties(followUp, vo);
        vo.setPlanDate(followUp.getPlanDate() == null ? null : followUp.getPlanDate().toString());
        vo.setActualDate(followUp.getActualDate() == null ? null : followUp.getActualDate().toString());
        vo.setImages(parseImages(followUp.getImages()));
        AdoptionRecord record = recordMapper.selectById(followUp.getRecordId());
        if (record != null) {
            vo.setRecordId(record.getId());
            vo.setApplicationId(record.getApplicationId());
            vo.setUserId(record.getUserId());
            vo.setPetId(record.getPetId());
            Pet pet = petMapper.selectById(record.getPetId());
            vo.setPetName(pet == null ? null : pet.getName());
            if (!ownerView) {
                User adopter = userMapper.selectById(record.getUserId());
                vo.setUserNickname(adopter == null ? null : adopter.getNickname());
            }
        }
        return vo;
    }

    private String petName(Long petId) {
        Pet pet = petMapper.selectById(petId);
        return pet == null ? "" : pet.getName();
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
}
