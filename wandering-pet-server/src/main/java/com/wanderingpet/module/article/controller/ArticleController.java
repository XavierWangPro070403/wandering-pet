package com.wanderingpet.module.article.controller;

import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.article.dto.ArticleQueryDTO;
import com.wanderingpet.module.article.dto.CommentCreateDTO;
import com.wanderingpet.module.article.service.ArticleService;
import com.wanderingpet.module.article.vo.ArticleDetailVO;
import com.wanderingpet.module.article.vo.ArticleListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资讯接口（门户）
 */
@Tag(name = "资讯科普（门户）", description = "文章列表、详情、点赞、评论")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章分页（已发布）")
    @GetMapping("/articles")
    public R<PageResult<ArticleListVO>> page(ArticleQueryDTO query) {
        return R.ok(articleService.pagePortal(query));
    }

    @Operation(summary = "文章详情（含评论）")
    @GetMapping("/articles/{id}")
    public R<ArticleDetailVO> detail(@PathVariable Long id) {
        return R.ok(articleService.detail(id));
    }

    @Operation(summary = "点赞")
    @PostMapping("/articles/{id}/like")
    public R<Void> like(@PathVariable Long id) {
        articleService.like(id);
        return R.ok();
    }

    @Operation(summary = "发表评论（先审后显）")
    @PostMapping("/comments")
    public R<Long> comment(@Valid @RequestBody CommentCreateDTO dto) {
        return R.ok(articleService.createComment(dto));
    }
}
