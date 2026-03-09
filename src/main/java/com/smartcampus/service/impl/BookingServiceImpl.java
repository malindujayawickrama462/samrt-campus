package com.smartcampus.service.impl;

import com.smartcampus.dto.request.BookingRequest;
import com.smartcampus.dto.request.BookingReviewRequest;
import com.smartcampus.dto.response.BookingResponse;
import com.smartcampus.entity.Booking;
import com.smartcampus.entity.Resource;
import com.smartcampus.entity.User;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.NotificationType;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.BookingConflictException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.BookingService;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public BookingResponse createBooking(BookingRequest req, Long userId) {
        Resource resource = resourceRepository.findById(req.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + req.getResourceId()));

        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            throw new BadRequestException("Resource is not available for booking");
        }

        if (req.getStartTime().isAfter(req.getEndTime()) || req.getStartTime().equals(req.getEndTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // Conflict check
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                req.getResourceId(), req.getBookingDate(), req.getStartTime(), req.getEndTime());
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("This resource is already booked for the selected time slot");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Booking booking = Booking.builder()
                .resource(resource)
                .user(user)
                .bookingDate(req.getBookingDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .purpose(req.getPurpose())
                .expectedAttendees(req.getExpectedAttendees())
                .status(BookingStatus.PENDING)
                .build();

        return toResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse reviewBooking(Long bookingId, BookingReviewRequest req, Long adminId) {
        Booking booking = findById(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be reviewed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        booking.setStatus(req.getApproved() ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking.setAdminReason(req.getReason());
        booking.setReviewedBy(admin);
        booking.setReviewedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        // Send notification
        NotificationType type = req.getApproved() ? NotificationType.BOOKING_APPROVED : NotificationType.BOOKING_REJECTED;
        String msg = req.getApproved()
                ? "Your booking for " + booking.getResource().getName() + " on " + booking.getBookingDate() + " has been approved."
                : "Your booking for " + booking.getResource().getName() + " was rejected. Reason: " + req.getReason();
        notificationService.sendNotification(booking.getUser(), type, msg, bookingId, "BOOKING");

        return toResponse(saved);
    }

    @Override
    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = findById(bookingId);

        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED) {
            throw new BadRequestException("Booking is already " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, Long requesterId) {
        return toResponse(findById(bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long userId, BookingStatus status) {
        List<Booking> bookings = status != null
                ? bookingRepository.findByUserIdAndStatus(userId, status)
                : bookingRepository.findByUserId(userId);
        return bookings.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(BookingStatus status) {
        List<Booking> bookings = status != null
                ? bookingRepository.findByStatus(status)
                : bookingRepository.findAll();
        return bookings.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    public BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .resourceId(b.getResource().getId())
                .resourceName(b.getResource().getName())
                .resourceLocation(b.getResource().getLocation())
                .userId(b.getUser().getId())
                .userName(b.getUser().getName())
                .bookingDate(b.getBookingDate())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .purpose(b.getPurpose())
                .expectedAttendees(b.getExpectedAttendees())
                .status(b.getStatus())
                .adminReason(b.getAdminReason())
                .reviewedBy(b.getReviewedBy() != null ? b.getReviewedBy().getName() : null)
                .reviewedAt(b.getReviewedAt())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
