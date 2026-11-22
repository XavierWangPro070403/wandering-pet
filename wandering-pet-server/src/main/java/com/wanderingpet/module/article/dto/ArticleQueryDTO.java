package com.wanderingpet.module.article.dto;

import lombok.Data;

/**
 * 文章列表查询入参
 */
@Data
public class ArticleQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /** RAISE / KNOWLEDGE / NEWS / STORY */
    private String category;

    /** 管理端状态筛选 0/1/2 */
    private Integer status;

    private String keyword;
}
