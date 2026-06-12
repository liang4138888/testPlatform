package com.testplatform.modules.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.testplatform.common.response.ApiResponse;
import com.testplatform.modules.auth.support.CurrentUserContext;
import com.testplatform.modules.notification.dto.NotificationResponse;
import com.testplatform.modules.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> list() {
        return ApiResponse.ok(notificationService.list(CurrentUserContext.getUserId()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(notificationService.unreadCount(CurrentUserContext.getUserId()));
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long notificationId) {
        notificationService.markRead(notificationId, CurrentUserContext.getUserId());
        return ApiResponse.ok(null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead(CurrentUserContext.getUserId());
        return ApiResponse.ok(null);
    }
}
