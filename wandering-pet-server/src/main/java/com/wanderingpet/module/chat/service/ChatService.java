package com.wanderingpet.module.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.module.chat.vo.MessageVO;
import com.wanderingpet.module.chat.vo.SessionVO;

import java.util.List;

/**
 * 在线咨询服务（会话存 MySQL，消息存 MongoDB）
 */
public interface ChatService {

    /** 获取或创建会话（用户与机构） */
    SessionVO getOrCreateSession(Long shelterId);

    /** 会话列表（用户视角 / 机构视角） */
    List<SessionVO> mySessions();

    /** 消息分页（倒序翻页，进入会话自动已读） */
    Page<MessageVO> pageMessages(Long sessionId, Integer pageNum, Integer pageSize);

    /** 发送消息 */
    void sendMessage(Long sessionId, String contentType, String content, String objectUrl);

    /**
     * 领养申请自动建联：申请提交后自动创建（或复用）用户与发布机构的会话，
     * 并发送一条结构化「申请卡片」消息（Boss 直聘式），机构在线经 WebSocket 实时收到。
     */
    void sendApplicationCard(Long userId, Long shelterId, Long applicationId);

    /** 标记会话已读（对端视角） */
    void markRead(Long sessionId);

    /** 未读总数 */
    Long unreadTotal();

    /** 撤回消息：仅发送者本人、2 分钟内可撤回，撤回后双方都不再看到原内容 */
    void recallMessage(String messageId);

    /** 删除消息：仅对当前登录方隐藏（像微信「删除」，对方不受影响） */
    void deleteMessage(String messageId);
}
