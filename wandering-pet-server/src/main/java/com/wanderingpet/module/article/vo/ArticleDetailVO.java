package com.wanderingpet.module.article.vo;

import lombok.Data;

import java.util.List;

/**
 * 文章详情（含评论）
 */
@Data
public class ArticleDetailVO {

    private Long id;

    private String title;

    private String cover;

    private String category;

    private String summary;

    /** 富文本 HTML（白名单过滤后） */
    private String content;

    private String author;

    private Integer viewCount;

    private Integer likeCount;

    private Integer isTop;

    private String publishTime;

    /** 当前登录用户是否已点赞 */
    private Boolean liked;

    /** 评论列表（仅已通过） */
    private List<CommentVO> comments;
}
