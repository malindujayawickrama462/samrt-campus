package com.smartcampus.controller;

import com.smartcampus.dto.request.CommentRequest;
import com.smartcampus.dto.request.TicketRequest;
import com.smartcampus.dto.request.TicketStatusRequest;
import com.smartcampus.dto.response.CommentResponse;
import com.smartcampus.dto.response.TicketResponse;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // POST /api/tickets – create ticket (USER)
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request,
                                                       Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request, userId));
    }

    // GET /api/tickets/my
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            @RequestParam(required = false) TicketStatus status,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ticketService.getMyTickets(userId, status));
    }

    // GET /api/tickets – all (ADMIN/TECHNICIAN)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ticketService.getAllTickets(status, priority, category));
    }

    // GET /api/tickets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    // PATCH /api/tickets/{id}/status – update status (ADMIN/TECHNICIAN)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody TicketStatusRequest request,
                                                       Authentication auth) {
        Long requesterId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request, requesterId));
    }

    // POST /api/tickets/{id}/attachments – upload images (max 3)
    @PostMapping("/{id}/attachments")
    public ResponseEntity<Void> addAttachments(@PathVariable Long id,
                                               @RequestParam("files") List<MultipartFile> files,
                                               Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        ticketService.addAttachments(id, files, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/tickets/{id}/comments
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getComments(id));
    }

    // POST /api/tickets/{id}/comments
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id,
                                                      @Valid @RequestBody CommentRequest request,
                                                      Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addComment(id, request, userId));
    }

    // PUT /api/tickets/comments/{commentId}
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long commentId,
                                                       @Valid @RequestBody CommentRequest request,
                                                       Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ticketService.editComment(commentId, request, userId));
    }

    // DELETE /api/tickets/comments/{commentId}
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        ticketService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
