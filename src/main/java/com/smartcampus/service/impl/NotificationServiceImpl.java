package com.smartcampus.service.impl;

import com.smartcampus.dto.response.NotificationResponse;
import com.smartcampus.entity.Notification;
import com.smartcampus.entity.User;
import com.smartcampus.enums.NotificationType;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.NotificationRepository;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Async
    public void sendNotification(User user, NotificationType type, String message, Long referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot access this notification");
        }
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .read(n.isRead())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
