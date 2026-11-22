package com.wanderingpet.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.notification.entity.Notification;
import com.wanderingpet.module.notification.mapper.NotificationMapper;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.notification.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 站内消息服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public void send(Long userId, String userType, String title, String content, String bizType, Long bizId) {
        if (userId == null || userType == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setUserType(userType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBizType(bizType == null ? "SYSTEM" : bizType);
        notification.setBizId(bizId == null ? 0L : bizId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    public com.wanderingpet.common.result.PageResult<NotificationVO> pageMy(Integer pageNum, Integer pageSize, Boolean onlyUnread) {
        LoginUser user = UserContext.current();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, user.getUserId())
                .eq(Notification::getUserType, user.getUserType())
                .eq(Boolean.TRUE.equals(onlyUnread), Notification::getIsRead, 0)
                .orderByDesc(Notification::getCreateTime);
        Page<Notification> page = notificationMapper.selectPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : Math.min(pageSize, 100)), wrapper);
        return com.wanderingpet.common.result.PageResult.of(page, source -> {
            NotificationVO vo = new NotificationVO();
            BeanUtils.copyProperties(source, vo);
            return vo;
        });
    }

    @Override
    public void markRead(Long id) {
        LoginUser user = UserContext.current();
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !Objects.equals(notification.getUserId(), user.getUserId())
                || !Objects.equals(notification.getUserType(), user.getUserType())) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        if (notification.getIsRead() == null || notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public void markAllRead() {
        LoginUser user = UserContext.current();
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, user.getUserId())
                .eq(Notification::getUserType, user.getUserType())
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadTime, LocalDateTime.now()));
    }

    @Override
    public Long unreadCount() {
        LoginUser user = UserContext.current();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, user.getUserId())
                .eq(Notification::getUserType, user.getUserType())
                .eq(Notification::getIsRead, 0));
    }
}
