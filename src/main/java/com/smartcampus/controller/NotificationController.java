package com.smartcampus.controller;

import com.smartcampus.dto.response.NotificationResponse;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications – get all for current user
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    // GET /api/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(userId)));
    }

    // PATCH /api/notifications/{id}/read – mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    // PATCH /api/notifications/read-all – mark all as read
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("updated", count));
    }
}
