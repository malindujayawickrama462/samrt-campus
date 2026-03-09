package com.smartcampus.controller;

import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.dto.response.UserResponse;
import com.smartcampus.entity.User;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build());
    }

    // Called from frontend after OAuth2 redirect with token
    @PostMapping("/token/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestParam String token) {
        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        Long userId = tokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .pictureUrl(user.getPictureUrl())
                .role(user.getRole())
                .build());
    }
}
