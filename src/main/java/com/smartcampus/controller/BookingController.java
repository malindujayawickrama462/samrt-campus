package com.smartcampus.controller;

import com.smartcampus.dto.request.BookingRequest;
import com.smartcampus.dto.request.BookingReviewRequest;
import com.smartcampus.dto.response.BookingResponse;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // POST /api/bookings – create booking (USER)
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
                                                         Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request, userId));
    }

    // GET /api/bookings/my – get own bookings (USER)
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(bookingService.getMyBookings(userId, status));
    }

    // GET /api/bookings – all bookings (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) BookingStatus status) {
        return ResponseEntity.ok(bookingService.getAllBookings(status));
    }

    // GET /api/bookings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(bookingService.getBookingById(id, userId));
    }

    // PATCH /api/bookings/{id}/review – approve or reject (ADMIN)
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> reviewBooking(@PathVariable Long id,
                                                         @Valid @RequestBody BookingReviewRequest request,
                                                         Authentication auth) {
        Long adminId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(bookingService.reviewBooking(id, request, adminId));
    }

    // PATCH /api/bookings/{id}/cancel – cancel own booking (USER)
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(bookingService.cancelBooking(id, userId));
    }
}
