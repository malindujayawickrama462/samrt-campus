package com.smartcampus;

import com.smartcampus.dto.request.BookingRequest;
import com.smartcampus.entity.Resource;
import com.smartcampus.entity.User;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.enums.Role;
import com.smartcampus.exception.BookingConflictException;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.NotificationService;
import com.smartcampus.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ResourceRepository resourceRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private BookingServiceImpl bookingService;

    private Resource resource;
    private User user;

    @BeforeEach
    void setUp() {
        resource = Resource.builder()
                .id(1L).name("Lab 101").type(ResourceType.LAB)
                .location("Block A").status(ResourceStatus.ACTIVE).build();

        user = User.builder()
                .id(1L).name("John").email("john@example.com").role(Role.USER).build();
    }

    @Test
    void createBooking_Success() {
        BookingRequest req = new BookingRequest();
        req.setResourceId(1L);
        req.setBookingDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(10, 0));
        req.setPurpose("Lecture");

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), any()))
                .thenReturn(List.of());
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            var b = inv.getArgument(0, com.smartcampus.entity.Booking.class);
            b = com.smartcampus.entity.Booking.builder()
                    .id(1L).resource(resource).user(user)
                    .bookingDate(req.getBookingDate()).startTime(req.getStartTime())
                    .endTime(req.getEndTime()).purpose(req.getPurpose())
                    .status(BookingStatus.PENDING).build();
            return b;
        });

        var result = bookingService.createBooking(req, 1L);
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    @Test
    void createBooking_ConflictThrowsException() {
        BookingRequest req = new BookingRequest();
        req.setResourceId(1L);
        req.setBookingDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(10, 0));
        req.setPurpose("Test");

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        com.smartcampus.entity.Booking existing = com.smartcampus.entity.Booking.builder()
                .id(2L).resource(resource).user(user)
                .bookingDate(req.getBookingDate())
                .startTime(LocalTime.of(9, 30)).endTime(LocalTime.of(10, 30))
                .status(BookingStatus.APPROVED).build();

        when(bookingRepository.findConflictingBookings(anyLong(), any(), any(), any()))
                .thenReturn(List.of(existing));

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(req, 1L));
    }

    @Test
    void createBooking_InvalidTimeThrowsException() {
        BookingRequest req = new BookingRequest();
        req.setResourceId(1L);
        req.setBookingDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(11, 0));
        req.setEndTime(LocalTime.of(9, 0)); // end before start

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        assertThrows(com.smartcampus.exception.BadRequestException.class,
                () -> bookingService.createBooking(req, 1L));
    }
}
