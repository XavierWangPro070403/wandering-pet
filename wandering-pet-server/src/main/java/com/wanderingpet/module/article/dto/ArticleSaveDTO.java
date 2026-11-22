package com.wanderingpet.module.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文章保存入参（管理端）
 */
@Data
public class ArticleSaveDTO {

    @NotBlank(message = "请输入标题")
    @Size(max = 128)
    private String title;

    private String cover;

    @NotBlank(message = "请选择分类")
    private String category;

    @Size(max = 255)
    private String summary;

    @NotBlank(message = "请输入正文")
    private String content;

    /** 是否置顶 */
    private Integer isTop;

    /** 保存动作：0-存草稿 1-发布 */
    private Integer status;
}
