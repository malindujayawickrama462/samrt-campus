package com.smartcampus.repository;

import com.smartcampus.entity.Booking;
import com.smartcampus.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByResourceId(Long resourceId);

    // Conflict check: same resource, same date, overlapping time, not cancelled/rejected
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.bookingDate = :date " +
           "AND b.status IN (com.smartcampus.enums.BookingStatus.PENDING, com.smartcampus.enums.BookingStatus.APPROVED) " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findConflictingBookings(
            @Param("resourceId") Long resourceId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Same as above but exclude a specific booking (for updates)
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.bookingDate = :date " +
           "AND b.id != :excludeId " +
           "AND b.status IN (com.smartcampus.enums.BookingStatus.PENDING, com.smartcampus.enums.BookingStatus.APPROVED) " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findConflictingBookingsExcluding(
            @Param("resourceId") Long resourceId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
