package com.wanderingpet.module.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.result.PageResult;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.notification.dto.BroadcastDTO;
import com.wanderingpet.module.notification.entity.Notification;
import com.wanderingpet.module.notification.mapper.NotificationMapper;
import com.wanderingpet.module.notification.service.NotificationService;
import com.wanderingpet.module.notification.vo.NotificationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息中心接口
 */
@Tag(name = "消息中心", description = "站内消息、已读、批量推送")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @Operation(summary = "我的消息分页")
    @GetMapping("/notifications")
    public R<PageResult<NotificationVO>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) Boolean onlyUnread) {
        return R.ok(notificationService.pageMy(pageNum, pageSize, onlyUnread));
    }

    @Operation(summary = "标记已读")
    @PostMapping("/notifications/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        notificationService.markRead(id);
        return R.ok();
    }

    @Operation(summary = "全部已读")
    @PostMapping("/notifications/read-all")
    public R<Void> readAll() {
        notificationService.markAllRead();
        return R.ok();
    }

    @Operation(summary = "未读数")
    @GetMapping("/notifications/unread-count")
    public R<Long> unreadCount() {
        return R.ok(notificationService.unreadCount());
    }

    @Operation(summary = "批量推送（管理员）")
    @RequirePermission("notification:push")
    @PostMapping("/admin/notifications/broadcast")
    @OpLog(module = "notification", action = "批量推送消息")
    public R<Integer> broadcast(@Valid @RequestBody BroadcastDTO dto) {
        List<Notification> targets = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .select(Notification::getUserId)
                .eq(Notification::getUserType, dto.getUserType())
                .groupBy(Notification::getUserId));
        // groupBy 去重后逐个发送
        int count = 0;
        for (Notification target : targets) {
            notificationService.send(target.getUserId(), dto.getUserType(), dto.getTitle(), dto.getContent(),
                    "SYSTEM", 0L);
            count++;
        }
        return R.ok("已推送 " + count + " 人", count);
    }
}
