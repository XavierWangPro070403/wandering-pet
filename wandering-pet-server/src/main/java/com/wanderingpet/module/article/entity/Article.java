package com.wanderingpet.module.article.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanderingpet.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资讯科普文章
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {

    private String title;

    private String cover;

    /** RAISE / KNOWLEDGE / NEWS / STORY */
    private String category;

    private String summary;

    /** 富文本 HTML（保存前经 Jsoup 白名单过滤） */
    private String content;

    private String author;

    private Integer viewCount;

    private Integer likeCount;

    /** 是否置顶 */
    private Integer isTop;

    /** 0-草稿 1-已发布 2-已下架 */
    private Integer status;

    private LocalDateTime publishTime;
}
