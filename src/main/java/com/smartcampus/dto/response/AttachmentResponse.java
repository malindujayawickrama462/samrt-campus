package com.smartcampus.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
