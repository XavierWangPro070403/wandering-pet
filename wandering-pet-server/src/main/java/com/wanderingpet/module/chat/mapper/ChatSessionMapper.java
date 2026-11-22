package com.wanderingpet.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.chat.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话 Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
