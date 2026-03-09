package com.smartcampus.dto.request;

import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ResourceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Type is required")
    private ResourceType type;

    @NotBlank(message = "Location is required")
    private String location;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private LocalTime availableFrom;

    private LocalTime availableTo;

    private ResourceStatus status;
}
