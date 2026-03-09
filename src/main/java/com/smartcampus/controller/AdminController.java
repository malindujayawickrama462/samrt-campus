package com.smartcampus.controller;

import com.smartcampus.dto.response.UserResponse;
import com.smartcampus.entity.User;
import com.smartcampus.enums.Role;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return ResponseEntity.ok(toResponse(user));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                    @RequestParam Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setRole(role);
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    @PatchMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleActive(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(!user.isActive());
        return ResponseEntity.ok(toResponse(userRepository.save(user)));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .name(u.getName())
                .pictureUrl(u.getPictureUrl())
                .role(u.getRole())
                .active(u.isActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
