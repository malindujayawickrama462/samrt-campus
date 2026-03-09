package com.smartcampus.repository;

import com.smartcampus.entity.Ticket;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByReporterId(Long reporterId);

    List<Ticket> findByAssignedToId(Long technicianId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByPriority(TicketPriority priority);

    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:category IS NULL OR LOWER(t.category) = LOWER(:category))")
    List<Ticket> searchTickets(
            @Param("status") TicketStatus status,
            @Param("priority") TicketPriority priority,
            @Param("category") String category
    );
}
