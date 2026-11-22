package com.wanderingpet.module.article.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论审核入参
 */
@Data
public class CommentAuditDTO {

    /** true-通过 false-屏蔽 */
    @NotNull(message = "请选择审核结果")
    private Boolean pass;
}
