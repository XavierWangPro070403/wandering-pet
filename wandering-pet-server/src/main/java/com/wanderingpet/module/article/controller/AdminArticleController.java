package com.wanderingpet.module.article.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.context.UserContext;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.article.dto.ArticleQueryDTO;
import com.wanderingpet.module.article.dto.ArticleSaveDTO;
import com.wanderingpet.module.article.dto.CommentAuditDTO;
import com.wanderingpet.module.article.service.ArticleService;
import com.wanderingpet.module.article.vo.ArticleListVO;
import com.wanderingpet.module.article.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端内容接口（文章 CRUD + 评论审核）
 */
@Tag(name = "管理端-内容管理", description = "资讯文章增删改、上下架、置顶、评论审核")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章分页（全状态）")
    @RequirePermission("article:manage")
    @GetMapping("/articles")
    public R<PageResult<ArticleListVO>> page(ArticleQueryDTO query) {
        return R.ok(articleService.pageAdmin(query));
    }

    @Operation(summary = "新增文章")
    @RequirePermission("article:manage")
    @PostMapping("/articles")
    @OpLog(module = "article", action = "新增文章")
    public R<Long> save(@Valid @RequestBody ArticleSaveDTO dto) {
        var user = UserContext.current();
        return R.ok(articleService.save(dto, user == null ? "平台运营组" : user.getUsername()));
    }

    @Operation(summary = "更新文章")
    @RequirePermission("article:manage")
    @PutMapping("/articles/{id}")
    @OpLog(module = "article", action = "更新文章")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveDTO dto) {
        articleService.update(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除文章")
    @RequirePermission("article:manage")
    @DeleteMapping("/articles/{id}")
    @OpLog(module = "article", action = "删除文章")
    public R<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return R.ok();
    }

    @Operation(summary = "置顶/取消置顶")
    @RequirePermission("article:manage")
    @PostMapping("/articles/{id}/top")
    public R<Void> top(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        articleService.changeTop(id, Boolean.TRUE.equals(body.get("top"))
                || "true".equals(String.valueOf(body.get("top"))));
        return R.ok();
    }

    @Operation(summary = "上下架")
    @RequirePermission("article:manage")
    @PostMapping("/articles/{id}/status")
    public R<Void> status(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        articleService.changeStatus(id, body.get("status"));
        return R.ok();
    }

    @Operation(summary = "评论分页（审核）")
    @RequirePermission("comment:audit")
    @GetMapping("/comments")
    public R<PageResult<CommentVO>> comments(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) String targetType) {
        return R.ok(articleService.pageCommentsAdmin(pageNum, pageSize, status, targetType));
    }

    @Operation(summary = "评论审核（通过/屏蔽）")
    @RequirePermission("comment:audit")
    @PostMapping("/comments/{id}/audit")
    @OpLog(module = "comment", action = "评论审核")
    public R<Void> auditComment(@PathVariable Long id, @RequestBody CommentAuditDTO dto) {
        articleService.auditComment(id, dto);
        return R.ok();
    }

    @Operation(summary = "删除评论")
    @RequirePermission("comment:audit")
    @DeleteMapping("/comments/{id}")
    public R<Void> deleteComment(@PathVariable Long id) {
        articleService.deleteComment(id);
        return R.ok();
    }
}
