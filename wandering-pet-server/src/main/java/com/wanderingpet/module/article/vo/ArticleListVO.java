package com.wanderingpet.module.article.vo;

import lombok.Data;

/**
 * 文章列表项
 */
@Data
public class ArticleListVO {

    private Long id;

    private String title;

    private String cover;

    private String category;

    private String summary;

    private String author;

    private Integer viewCount;

    private Integer likeCount;

    private Integer isTop;

    private Integer status;

    private String publishTime;

    private String createTime;
}
