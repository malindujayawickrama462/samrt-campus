package com.smartcampus.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String authorPicture;
    private String content;
    private boolean edited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
