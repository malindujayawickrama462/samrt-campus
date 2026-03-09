package com.smartcampus.dto.response;

import com.smartcampus.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String pictureUrl;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
