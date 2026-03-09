package com.smartcampus.dto.response;

import com.smartcampus.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String name;
    private String email;
    private String pictureUrl;
    private Role role;
}
