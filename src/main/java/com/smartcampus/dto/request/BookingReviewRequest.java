package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingReviewRequest {
    @NotNull(message = "Approved flag is required")
    private Boolean approved;
    private String reason;
}
