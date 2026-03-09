package com.smartcampus.dto.request;

import com.smartcampus.enums.TicketPriority;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TicketRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private TicketPriority priority;

    private String location;

    @Email(message = "Invalid contact email")
    private String preferredContact;
}
