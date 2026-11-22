package com.wanderingpet.module.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户中心数字摘要出参
 */
@Data
@AllArgsConstructor
public class UserSummaryVO {

    /** 我的申请数 */
    private Long applicationCount;

    /** 进行中申请数 */
    private Long activeApplicationCount;

    /** 我的收藏数 */
    private Long favoriteCount;

    /** 我的上报数 */
    private Long reportCount;

    /** 未读消息数 */
    private Long unreadCount;
}
