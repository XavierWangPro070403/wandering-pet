package com.wanderingpet.module.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanderingpet.module.article.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资讯文章 Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
