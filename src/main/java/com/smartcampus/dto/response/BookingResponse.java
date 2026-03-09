package com.smartcampus.dto.response;

import com.smartcampus.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data @Builder
public class BookingResponse {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private String resourceLocation;
    private Long userId;
    private String userName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private Integer expectedAttendees;
    private BookingStatus status;
    private String adminReason;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
