package com.wanderingpet.module.article.vo;

import lombok.Data;

/**
 * 评论出参
 */
@Data
public class CommentVO {

    private Long id;

    private String targetType;

    private Long targetId;

    private Long parentId;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private String content;

    private Integer likeCount;

    /** 0-待审核 1-已通过 2-已屏蔽 */
    private Integer status;

    private String createTime;
}
