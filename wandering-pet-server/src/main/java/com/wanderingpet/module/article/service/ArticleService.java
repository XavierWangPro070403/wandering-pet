package com.wanderingpet.module.article.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.module.article.dto.ArticleQueryDTO;
import com.wanderingpet.module.article.dto.ArticleSaveDTO;
import com.wanderingpet.module.article.dto.CommentAuditDTO;
import com.wanderingpet.module.article.dto.CommentCreateDTO;
import com.wanderingpet.module.article.vo.ArticleDetailVO;
import com.wanderingpet.module.article.vo.ArticleListVO;
import com.wanderingpet.module.article.vo.CommentVO;

/**
 * 资讯 / 评论服务
 */
public interface ArticleService {

    /** 门户文章分页（已发布，置顶优先） */
    PageResult<ArticleListVO> pagePortal(ArticleQueryDTO query);

    /** 文章详情（浏览量 +1，返回已通过评论） */
    ArticleDetailVO detail(Long id);

    /** 点赞（登录用户，Redis Set 去重，幂等） */
    void like(Long id);

    // ---------- 管理端 ----------

    PageResult<ArticleListVO> pageAdmin(ArticleQueryDTO query);

    Long save(ArticleSaveDTO dto, String author);

    void update(Long id, ArticleSaveDTO dto);

    void delete(Long id);

    void changeTop(Long id, Boolean top);

    void changeStatus(Long id, Integer status);

    // ---------- 评论 ----------

    Long createComment(CommentCreateDTO dto);

    PageResult<CommentVO> pageCommentsAdmin(Integer pageNum, Integer pageSize, Integer status, String targetType);

    void auditComment(Long id, CommentAuditDTO dto);

    void deleteComment(Long id);
}
