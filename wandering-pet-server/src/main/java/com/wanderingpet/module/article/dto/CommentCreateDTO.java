package com.wanderingpet.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论入参
 */
@Data
public class CommentCreateDTO {

    @NotBlank(message = "评论对象类型不能为空")
    private String targetType;

    @NotNull(message = "评论对象不能为空")
    private Long targetId;

    /** 父评论 ID（0-一级评论） */
    private Long parentId;

    @NotBlank(message = "请输入评论内容")
    @Size(max = 500, message = "评论最长 500 字")
    private String content;
}
