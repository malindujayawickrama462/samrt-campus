package com.smartcampus.dto.response;

import com.smartcampus.enums.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String message;
    private boolean read;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
