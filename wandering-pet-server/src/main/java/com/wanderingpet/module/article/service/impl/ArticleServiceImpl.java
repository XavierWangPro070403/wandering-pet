package com.wanderingpet.module.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.constant.BizConstant;
import com.wanderingpet.common.constant.RedisKeyConstant;
import com.wanderingpet.common.context.LoginUser;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.common.util.RedisUtil;
import com.wanderingpet.module.article.dto.ArticleQueryDTO;
import com.wanderingpet.module.article.dto.ArticleSaveDTO;
import com.wanderingpet.module.article.dto.CommentAuditDTO;
import com.wanderingpet.module.article.dto.CommentCreateDTO;
import com.wanderingpet.module.article.entity.Article;
import com.wanderingpet.module.article.entity.Comment;
import com.wanderingpet.module.article.mapper.ArticleMapper;
import com.wanderingpet.module.article.mapper.CommentMapper;
import com.wanderingpet.module.article.service.ArticleService;
import com.wanderingpet.module.article.vo.ArticleDetailVO;
import com.wanderingpet.module.article.vo.ArticleListVO;
import com.wanderingpet.module.article.vo.CommentVO;
import com.wanderingpet.module.user.entity.User;
import com.wanderingpet.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 资讯 / 评论服务实现
 *
 * <p>富文本入库前经 Jsoup 白名单过滤（p/h3/ul/ol/li/strong/em/br/img/a）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    // ---------------------------------------------------------------- 门户

    @Override
    public PageResult<ArticleListVO> pagePortal(ArticleQueryDTO query) {
        Page<Article> page = articleMapper.selectPage(new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, BizConstant.STATUS_PUBLISHED)
                        .eq(query.getCategory() != null && !query.getCategory().isBlank(), Article::getCategory, query.getCategory())
                        .like(query.getKeyword() != null && !query.getKeyword().isBlank(), Article::getTitle, query.getKeyword())
                        .orderByDesc(Article::getIsTop)
                        .orderByDesc(Article::getPublishTime));
        return PageResult.of(page, this::toListVO);
    }

    @Override
    public ArticleDetailVO detail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() != BizConstant.STATUS_PUBLISHED) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .setSql("view_count = view_count + 1"));

        ArticleDetailVO vo = new ArticleDetailVO();
        org.springframework.beans.BeanUtils.copyProperties(article, vo);
        vo.setPublishTime(article.getPublishTime() == null ? null : article.getPublishTime().toString().replace('T', ' '));
        LoginUser user = UserContext.current();
        vo.setLiked(user != null && user.isUser()
                && redisUtil.setIsMember(RedisKeyConstant.LIKE_ARTICLE.formatted(id), String.valueOf(user.getUserId())));
        // 评论（仅已通过，一级+回复平铺）
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, BizConstant.TARGET_ARTICLE)
                .eq(Comment::getTargetId, id)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime));
        Map<Long, User> users = usersOf(comments.stream().map(Comment::getUserId).toList());
        vo.setComments(comments.stream().map(c -> toCommentVO(c, users)).toList());
        return vo;
    }

    @Override
    public void like(Long id) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        String key = RedisKeyConstant.LIKE_ARTICLE.formatted(id);
        String member = String.valueOf(user.getUserId());
        if (redisUtil.setIsMember(key, member)) {
            return;
        }
        redisUtil.setAdd(key, member, Duration.ofDays(7));
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .setSql("like_count = like_count + 1"));
    }

    // ---------------------------------------------------------------- 管理端

    @Override
    public PageResult<ArticleListVO> pageAdmin(ArticleQueryDTO query) {
        Page<Article> page = articleMapper.selectPage(new Page<>(query.getPageNum(), Math.min(query.getPageSize(), 100)),
                new LambdaQueryWrapper<Article>()
                        .eq(query.getStatus() != null, Article::getStatus, query.getStatus())
                        .eq(query.getCategory() != null && !query.getCategory().isBlank(), Article::getCategory, query.getCategory())
                        .like(query.getKeyword() != null && !query.getKeyword().isBlank(), Article::getTitle, query.getKeyword())
                        .orderByDesc(Article::getUpdateTime));
        return PageResult.of(page, this::toListVO);
    }

    @Override
    public Long save(ArticleSaveDTO dto, String author) {
        Article article = new Article();
        org.springframework.beans.BeanUtils.copyProperties(dto, article);
        article.setContent(sanitize(dto.getContent()));
        article.setAuthor(author == null ? "平台运营组" : author);
        article.setIsTop(Boolean.TRUE.equals(dto.getIsTop()) ? 1 : 0);
        article.setStatus(dto.getStatus() != null && dto.getStatus() == 1 ? BizConstant.STATUS_PUBLISHED : BizConstant.STATUS_DRAFT);
        if (article.getStatus() == BizConstant.STATUS_PUBLISHED) {
            article.setPublishTime(LocalDateTime.now());
        }
        article.setViewCount(0);
        article.setLikeCount(0);
        articleMapper.insert(article);
        return article.getId();
    }

    @Override
    public void update(Long id, ArticleSaveDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        if (dto.getTitle() != null) {
            article.setTitle(dto.getTitle());
        }
        if (dto.getCover() != null) {
            article.setCover(dto.getCover());
        }
        if (dto.getCategory() != null) {
            article.setCategory(dto.getCategory());
        }
        if (dto.getSummary() != null) {
            article.setSummary(dto.getSummary());
        }
        if (dto.getContent() != null) {
            article.setContent(sanitize(dto.getContent()));
        }
        if (dto.getIsTop() != null) {
            article.setIsTop(dto.getIsTop());
        }
        if (dto.getStatus() != null) {
            if (dto.getStatus() == BizConstant.STATUS_PUBLISHED && article.getPublishTime() == null) {
                article.setPublishTime(LocalDateTime.now());
            }
            article.setStatus(dto.getStatus());
        }
        articleMapper.updateById(article);
    }

    @Override
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }

    @Override
    public void changeTop(Long id, Boolean top) {
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .set(Article::getIsTop, Boolean.TRUE.equals(top) ? 1 : 0));
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw BizException.of(ResultCode.BAD_REQUEST, "状态不正确");
        }
        LambdaUpdateWrapper<Article> wrapper = new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .set(Article::getStatus, status);
        if (status == BizConstant.STATUS_PUBLISHED) {
            wrapper.set(Article::getPublishTime, LocalDateTime.now());
        }
        articleMapper.update(null, wrapper);
    }

    // ---------------------------------------------------------------- 评论

    @Override
    public Long createComment(CommentCreateDTO dto) {
        LoginUser user = UserContext.current();
        if (user == null || !user.isUser()) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        Comment comment = new Comment();
        comment.setTargetType(dto.getTargetType());
        comment.setTargetId(dto.getTargetId());
        comment.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        comment.setUserId(user.getUserId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(0);
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public PageResult<CommentVO> pageCommentsAdmin(Integer pageNum, Integer pageSize, Integer status, String targetType) {
        Page<Comment> page = commentMapper.selectPage(new Page<>(pageNum, Math.min(pageSize, 100)),
                new LambdaQueryWrapper<Comment>()
                        .eq(status != null, Comment::getStatus, status)
                        .eq(targetType != null && !targetType.isBlank(), Comment::getTargetType, targetType)
                        .orderByDesc(Comment::getCreateTime));
        Map<Long, User> users = usersOf(page.getRecords().stream().map(Comment::getUserId).toList());
        List<CommentVO> list = page.getRecords().stream().map(c -> toCommentVO(c, users)).toList();
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public void auditComment(Long id, CommentAuditDTO dto) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw BizException.of(ResultCode.NOT_FOUND);
        }
        comment.setStatus(Boolean.TRUE.equals(dto.getPass()) ? 1 : 2);
        commentMapper.updateById(comment);
    }

    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }

    // ---------------------------------------------------------------- private

    private ArticleListVO toListVO(Article article) {
        ArticleListVO vo = new ArticleListVO();
        org.springframework.beans.BeanUtils.copyProperties(article, vo);
        vo.setPublishTime(article.getPublishTime() == null ? null : article.getPublishTime().toString().replace('T', ' '));
        vo.setCreateTime(article.getCreateTime() == null ? null : article.getCreateTime().toString().replace('T', ' '));
        return vo;
    }

    private CommentVO toCommentVO(Comment comment, Map<Long, User> users) {
        CommentVO vo = new CommentVO();
        org.springframework.beans.BeanUtils.copyProperties(comment, vo);
        vo.setCreateTime(comment.getCreateTime() == null ? null : comment.getCreateTime().toString().replace('T', ' '));
        User user = users.get(comment.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        return vo;
    }

    private Map<Long, User> usersOf(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds.stream().distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
    }

    private String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.clean(html, Safelist.basicWithImages()
                .addTags("h3", "h4")
                .addAttributes("img", "src", "alt")
                .addAttributes("a", "href", "target"));
    }
}
