package com.wanderingpet.module.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.module.notification.vo.NotificationVO;

/**
 * 站内消息服务（各业务模块共用发送入口）
 */
public interface NotificationService {

    /**
     * 发送单条站内消息
     *
     * @param userId   接收人 ID
     * @param userType 接收人类型 USER/SHELTER/ADMIN
     * @param title    标题
     * @param content  内容
     * @param bizType  业务类型
     * @param bizId    业务 ID
     */
    void send(Long userId, String userType, String title, String content, String bizType, Long bizId);

    /** 分页查询当前用户消息 */
    com.wanderingpet.common.result.PageResult<NotificationVO> pageMy(Integer pageNum, Integer pageSize, Boolean onlyUnread);

    /** 标记单条已读（归属校验） */
    void markRead(Long id);

    /** 全部已读 */
    void markAllRead();

    /** 当前用户未读数 */
    Long unreadCount();
}
