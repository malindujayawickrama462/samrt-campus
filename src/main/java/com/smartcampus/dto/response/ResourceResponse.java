package com.smartcampus.dto.response;

import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data @Builder
public class ResourceResponse {
    private Long id;
    private String name;
    private ResourceType type;
    private String location;
    private Integer capacity;
    private String description;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private ResourceStatus status;
    private LocalDateTime createdAt;
}
