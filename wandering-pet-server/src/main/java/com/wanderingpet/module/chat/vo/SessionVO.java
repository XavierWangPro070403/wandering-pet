package com.wanderingpet.module.chat.vo;

import lombok.Data;

/**
 * 会话出参（用户侧 / 机构侧通用）
 */
@Data
public class SessionVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private Long shelterId;

    private String shelterName;

    private String shelterLogo;

    private String lastMessage;

    private String lastTime;

    /** 当前视角的未读数 */
    private Integer unread;
}
