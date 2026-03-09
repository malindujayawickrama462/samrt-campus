package com.smartcampus.service;

import com.smartcampus.dto.request.BookingRequest;
import com.smartcampus.dto.request.BookingReviewRequest;
import com.smartcampus.dto.response.BookingResponse;
import com.smartcampus.enums.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, Long userId);
    BookingResponse reviewBooking(Long bookingId, BookingReviewRequest request, Long adminId);
    BookingResponse cancelBooking(Long bookingId, Long userId);
    BookingResponse getBookingById(Long bookingId, Long requesterId);
    List<BookingResponse> getMyBookings(Long userId, BookingStatus status);
    List<BookingResponse> getAllBookings(BookingStatus status);
}
