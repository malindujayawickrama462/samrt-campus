package com.smartcampus.service;

import com.smartcampus.dto.response.NotificationResponse;
import com.smartcampus.entity.User;
import com.smartcampus.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    void sendNotification(User user, NotificationType type, String message, Long referenceId, String referenceType);
    List<NotificationResponse> getNotificationsForUser(Long userId);
    NotificationResponse markAsRead(Long notificationId, Long userId);
    int markAllAsRead(Long userId);
    long countUnread(Long userId);
}
