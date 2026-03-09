package com.smartcampus.dto.response;

import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private TicketPriority priority;
    private TicketStatus status;
    private String location;
    private String preferredContact;
    private Long reporterId;
    private String reporterName;
    private Long assignedToId;
    private String assignedToName;
    private String resolutionNotes;
    private String rejectionReason;
    private LocalDateTime resolvedAt;
    private List<AttachmentResponse> attachments;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
