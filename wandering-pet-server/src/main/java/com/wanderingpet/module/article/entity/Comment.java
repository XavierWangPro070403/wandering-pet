package com.wanderingpet.module.article.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论（文章/宠物通用）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {

    /** ARTICLE / PET */
    private String targetType;

    private Long targetId;

    private Long userId;

    /** 父评论 ID（0-一级评论） */
    private Long parentId;

    private String content;

    private Integer likeCount;

    /** 0-待审核 1-已通过 2-已屏蔽 */
    private Integer status;
}
