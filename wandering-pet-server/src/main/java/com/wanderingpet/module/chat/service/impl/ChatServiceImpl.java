package com.wanderingpet.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.AuthConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.module.chat.document.ChatMessage;
import com.wanderingpet.module.chat.entity.ChatSession;
import com.wanderingpet.module.chat.mapper.ChatSessionMapper;
import com.wanderingpet.module.chat.service.ChatService;
import com.wanderingpet.module.chat.vo.MessageVO;
import com.wanderingpet.module.chat.vo.SessionVO;
import com.wanderingpet.module.chat.ws.ChatPushService;
import com.wanderingpet.module.adoption.entity.AdoptionApplication;
import com.wanderingpet.module.adoption.mapper.AdoptionApplicationMapper;
import com.wanderingpet.module.pet.entity.Pet;
import com.wanderingpet.module.pet.mapper.PetMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wanderingpet.module.shelter.entity.Shelter;
import com.wanderingpet.module.shelter.mapper.ShelterMapper;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 在线咨询服务实现（消息正文存 MongoDB，未读数存 MySQL 会话行）
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ShelterMapper shelterMapper;
    private final UserMapper userMapper;
    private final MongoTemplate mongoTemplate;
    private final ChatPushService chatPushService;
    private final AdoptionApplicationMapper applicationMapper;
    private final PetMapper petMapper;
    private final ObjectMapper objectMapper;

    private LoginUser current() {
        LoginUser user = UserContext.current();
        if (user == null) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionVO getOrCreateSession(Long shelterId) {
        LoginUser user = current();
        if (!user.isUser()) {
            throw BizException.of(ResultCode.FORBIDDEN, "仅普通用户可发起咨询");
        }
        Shelter shelter = shelterMapper.selectById(shelterId);
        if (shelter == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        return toSessionVO(getOrCreateSessionRow(user.getUserId(), shelterId), true);
    }

    /** 会话行获取或创建（用户主动发起与申请自动建联共用） */
    private ChatSession getOrCreateSessionRow(Long userId, Long shelterId) {
        ChatSession session = sessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getShelterId, shelterId));
        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setShelterId(shelterId);
            session.setLastMessage("");
            session.setUserUnread(0);
            session.setShelterUnread(0);
            sessionMapper.insert(session);
        }
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendApplicationCard(Long userId, Long shelterId, Long applicationId) {
        ChatSession session = getOrCreateSessionRow(userId, shelterId);
        AdoptionApplication application = applicationMapper.selectById(applicationId);
        Pet pet = petMapper.selectById(application.getPetId());
        String reason = application.getReason();
        if (reason != null && reason.length() > 80) {
            reason = reason.substring(0, 80) + "…";
        }
        Map<String, Object> card = new HashMap<>();
        card.put("applicationId", String.valueOf(applicationId));
        card.put("applyNo", application.getApplyNo());
        card.put("petName", pet == null ? "" : pet.getName());
        card.put("petCover", pet == null ? "" : pet.getCoverImage());
        card.put("applicantName", application.getApplicantName());
        card.put("applicantPhone", application.getApplicantPhone());
        card.put("reason", reason == null ? "" : reason);
        try {
            sendRow(session, userId, AuthConstant.USER_TYPE_USER, shelterId,
                    "APPLICATION", objectMapper.writeValueAsString(card), null);
        } catch (JsonProcessingException e) {
            throw BizException.of(ResultCode.BAD_REQUEST, "申请卡片组装失败");
        }
    }

    /** 实际落库 + 未读数 + 实时推送（sendMessage 与自动建联卡片共用） */
    private void sendRow(ChatSession session, Long senderId, String senderType,
                         Long receiverId, String contentType, String content, String objectUrl) {
        boolean fromUser = AuthConstant.USER_TYPE_USER.equals(senderType);
        ChatMessage message = ChatMessage.builder()
                .sessionId(String.valueOf(session.getId()))
                .senderId(senderId)
                .senderType(senderType)
                .receiverId(receiverId)
                .contentType(contentType)
                .content(content == null ? "" : content)
                .objectUrl(objectUrl)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        mongoTemplate.save(message);
        session.setLastMessage("APPLICATION".equals(contentType) ? "📋 提交了领养申请"
                : "TEXT".equals(contentType) ? content : "[图片]");
        session.setLastTime(LocalDateTime.now());
        if (fromUser) {
            session.setShelterUnread(session.getShelterUnread() == null ? 1 : session.getShelterUnread() + 1);
        } else {
            session.setUserUnread(session.getUserUnread() == null ? 1 : session.getUserUnread() + 1);
        }
        sessionMapper.updateById(session);
        // 实时推送：接收方在线则即时收到，离线靠未读数兜底（寻址与 chat_session 一致）
        if (fromUser) {
            chatPushService.pushTo(AuthConstant.USER_TYPE_SHELTER, session.getShelterId(),
                    chatPushService.chatMessagePayload(toMessageVO(message)));
        } else {
            chatPushService.pushTo(AuthConstant.USER_TYPE_USER, session.getUserId(),
                    chatPushService.chatMessagePayload(toMessageVO(message)));
        }
    }

    @Override
    public List<SessionVO> mySessions() {
        LoginUser user = current();
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .orderByDesc(ChatSession::getLastTime)
                .orderByDesc(ChatSession::getId);
        if (user.isUser()) {
            wrapper.eq(ChatSession::getUserId, user.getUserId());
            return sessionMapper.selectList(wrapper).stream().map(s -> toSessionVO(s, true)).toList();
        }
        if (user.isShelter()) {
            wrapper.eq(ChatSession::getShelterId, user.getShelterId());
            return sessionMapper.selectList(wrapper).stream().map(s -> toSessionVO(s, false)).toList();
        }
        throw BizException.of(ResultCode.FORBIDDEN);
    }

    @Override
    public Page<MessageVO> pageMessages(Long sessionId, Integer pageNum, Integer pageSize) {
        LoginUser user = current();
        requireSession(sessionId, user);
        // 已删除的消息只对删除方隐藏
        String hideField = user.isUser() ? "deletedByUser" : "deletedByShelter";
        Criteria criteria = Criteria.where("sessionId").is(String.valueOf(sessionId))
                .and(hideField).ne(1);
        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createTime"))
                .skip((long) (pageNum - 1) * pageSize)
                .limit(pageSize);
        List<ChatMessage> messages = mongoTemplate.find(query, ChatMessage.class);
        long total = mongoTemplate.count(new Query(criteria), ChatMessage.class);
        List<MessageVO> list = messages.stream().map(this::toMessageVO).toList();
        Page<MessageVO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(list);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Long sessionId, String contentType, String content, String objectUrl) {
        LoginUser user = current();
        ChatSession session = requireSession(sessionId, user);
        if (!"TEXT".equals(contentType) && !"IMAGE".equals(contentType)) {
            throw BizException.of(ResultCode.BAD_REQUEST, "消息类型不正确");
        }
        if ("IMAGE".equals(contentType) && (objectUrl == null || objectUrl.isBlank())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "图片消息缺少 URL");
        }
        // 申请卡片走 sendApplicationCard 专用通道，防止用户伪造「申请卡片」消息
        sendRow(session, user.getUserId(),
                user.isUser() ? AuthConstant.USER_TYPE_USER : AuthConstant.USER_TYPE_SHELTER,
                user.isUser() ? session.getShelterId() : session.getUserId(),
                contentType, content, objectUrl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long sessionId) {
        LoginUser user = current();
        ChatSession session = requireSession(sessionId, user);
        boolean fromUser = user.isUser();
        mongoTemplate.updateMulti(
                new Query(Criteria.where("sessionId").is(String.valueOf(sessionId))
                        .and("senderType").is(fromUser ? AuthConstant.USER_TYPE_SHELTER : AuthConstant.USER_TYPE_USER)
                        .and("isRead").is(0)),
                Update.update("isRead", 1), ChatMessage.class);
        // 清零当前视角的未读数
        sessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getId, session.getId())
                .set(fromUser ? ChatSession::getShelterUnread : ChatSession::getUserUnread, 0));
    }

    @Override
    public Long unreadTotal() {
        LoginUser user = current();
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        if (user.isUser()) {
            wrapper.eq(ChatSession::getUserId, user.getUserId());
            List<ChatSession> sessions = sessionMapper.selectList(wrapper);
            return sessions.stream().mapToLong(s -> s.getUserUnread() == null ? 0 : s.getUserUnread()).sum();
        }
        if (user.isShelter()) {
            wrapper.eq(ChatSession::getShelterId, user.getShelterId());
            List<ChatSession> sessions = sessionMapper.selectList(wrapper);
            return sessions.stream().mapToLong(s -> s.getShelterUnread() == null ? 0 : s.getShelterUnread()).sum();
        }
        return 0L;
    }

    // ---------------------------------------------------------------- private

    /** 撤回窗口：2 分钟（与微信一致） */
    private static final long RECALL_WINDOW_MINUTES = 2;

    @Override
    public void recallMessage(String messageId) {
        LoginUser user = current();
        ChatMessage message = mongoTemplate.findById(messageId, ChatMessage.class);
        if (message == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "消息不存在");
        }
        ChatSession session = requireSession(Long.valueOf(message.getSessionId()), user);
        boolean mine = Objects.equals(message.getSenderId(), user.getUserId())
                && Objects.equals(message.getSenderType(), user.getUserType());
        if (!mine) {
            throw BizException.of(ResultCode.FORBIDDEN, "只能撤回自己发送的消息");
        }
        if (message.getRecalled() != null && message.getRecalled() == 1) {
            return;
        }
        if (message.getCreateTime() != null
                && message.getCreateTime().plusMinutes(RECALL_WINDOW_MINUTES).isBefore(LocalDateTime.now())) {
            throw BizException.of(ResultCode.BAD_REQUEST, "超过 2 分钟的消息不能撤回");
        }
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(messageId)),
                Update.update("recalled", 1).set("recalledAt", LocalDateTime.now()), ChatMessage.class);

        // 实时推送撤回事件给双方（对方立刻把气泡替换为「已撤回」）；寻址与 chat_session 一致
        chatPushService.pushTo(AuthConstant.USER_TYPE_USER, session.getUserId(),
                chatPushService.chatRecallPayload(message.getSessionId(), messageId));
        chatPushService.pushTo(AuthConstant.USER_TYPE_SHELTER, session.getShelterId(),
                chatPushService.chatRecallPayload(message.getSessionId(), messageId));

        // 被撤回的正好是最后一条时，会话列表预览也改成提示
        ChatMessage latest = mongoTemplate.findOne(
                new Query(Criteria.where("sessionId").is(message.getSessionId()))
                        .with(Sort.by(Sort.Direction.DESC, "createTime")).limit(1),
                ChatMessage.class);
        if (latest != null && Objects.equals(latest.getId(), messageId)) {
            session.setLastMessage("[消息已撤回]");
            sessionMapper.updateById(session);
        }
    }

    @Override
    public void deleteMessage(String messageId) {
        LoginUser user = current();
        ChatMessage message = mongoTemplate.findById(messageId, ChatMessage.class);
        if (message == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "消息不存在");
        }
        requireSession(Long.valueOf(message.getSessionId()), user);
        // 删除只对自己生效：对方聊天记录不受影响
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(messageId)),
                Update.update(user.isUser() ? "deletedByUser" : "deletedByShelter", 1),
                ChatMessage.class);
    }

    private ChatSession requireSession(Long sessionId, LoginUser user) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        boolean owner = (user.isUser() && Objects.equals(session.getUserId(), user.getUserId()))
                || (user.isShelter() && Objects.equals(session.getShelterId(), user.getShelterId()));
        if (!owner) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return session;
    }

    private SessionVO toSessionVO(ChatSession session, boolean userView) {
        SessionVO vo = new SessionVO();
        vo.setId(session.getId());
        vo.setUserId(session.getUserId());
        vo.setShelterId(session.getShelterId());
        vo.setLastMessage(session.getLastMessage());
        vo.setLastTime(session.getLastTime() == null ? null : session.getLastTime().toString().replace('T', ' '));
        vo.setUnread(userView ? session.getUserUnread() : session.getShelterUnread());
        User user = userMapper.selectById(session.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        Shelter shelter = shelterMapper.selectById(session.getShelterId());
        if (shelter != null) {
            vo.setShelterName(shelter.getName());
            vo.setShelterLogo(shelter.getLogo());
        }
        return vo;
    }

    private MessageVO toMessageVO(ChatMessage message) {
        MessageVO vo = new MessageVO();
        boolean recalled = message.getRecalled() != null && message.getRecalled() == 1;
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderId(message.getSenderId());
        vo.setSenderType(message.getSenderType());
        vo.setContentType(message.getContentType());
        // 已撤回：不再下发原始内容
        vo.setContent(recalled ? "" : message.getContent());
        vo.setObjectUrl(recalled ? null : message.getObjectUrl());
        vo.setIsRead(message.getIsRead());
        vo.setRecalled(recalled);
        if (message.getCreateTime() == null) {
            vo.setCreateTime(null);
        } else {
            // LocalDateTime.toString() 会带毫秒/纳秒，聊天时间只精确到秒
            String time = message.getCreateTime().toString().replace('T', ' ');
            vo.setCreateTime(time.length() > 19 ? time.substring(0, 19) : time);
        }
        return vo;
    }
}
