package com.smartcampus.service.impl;

import com.smartcampus.dto.request.CommentRequest;
import com.smartcampus.dto.request.TicketRequest;
import com.smartcampus.dto.request.TicketStatusRequest;
import com.smartcampus.dto.response.AttachmentResponse;
import com.smartcampus.dto.response.CommentResponse;
import com.smartcampus.dto.response.TicketResponse;
import com.smartcampus.entity.*;
import com.smartcampus.enums.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import com.smartcampus.service.NotificationService;
import com.smartcampus.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final NotificationService notificationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public TicketResponse createTicket(TicketRequest req, Long userId) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ticket ticket = Ticket.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .priority(req.getPriority() != null ? req.getPriority() : TicketPriority.MEDIUM)
                .location(req.getLocation())
                .preferredContact(req.getPreferredContact())
                .reporter(reporter)
                .status(TicketStatus.OPEN)
                .build();

        return toResponse(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse updateTicketStatus(Long ticketId, TicketStatusRequest req, Long requesterId) {
        Ticket ticket = findById(ticketId);
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Technician can only update to IN_PROGRESS or RESOLVED
        if (requester.getRole() == Role.TECHNICIAN &&
                req.getStatus() != TicketStatus.IN_PROGRESS && req.getStatus() != TicketStatus.RESOLVED) {
            throw new AccessDeniedException("Technicians can only set IN_PROGRESS or RESOLVED");
        }

        ticket.setStatus(req.getStatus());

        if (req.getResolutionNotes() != null) ticket.setResolutionNotes(req.getResolutionNotes());
        if (req.getRejectionReason() != null) ticket.setRejectionReason(req.getRejectionReason());
        if (req.getStatus() == TicketStatus.RESOLVED) ticket.setResolvedAt(LocalDateTime.now());

        if (req.getAssignedToId() != null) {
            User tech = userRepository.findById(req.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));
            ticket.setAssignedTo(tech);
            notificationService.sendNotification(tech, NotificationType.TICKET_ASSIGNED,
                    "You have been assigned to ticket: " + ticket.getTitle(), ticketId, "TICKET");
        }

        Ticket saved = ticketRepository.save(ticket);

        // Notify reporter
        notificationService.sendNotification(ticket.getReporter(), NotificationType.TICKET_STATUS_CHANGED,
                "Your ticket '" + ticket.getTitle() + "' status changed to " + req.getStatus(), ticketId, "TICKET");

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long ticketId) {
        return toResponse(findById(ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(Long userId, TicketStatus status) {
        List<Ticket> tickets = ticketRepository.findByReporterId(userId);
        if (status != null) tickets = tickets.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        return tickets.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(TicketStatus status, TicketPriority priority, String category) {
        return ticketRepository.searchTickets(status, priority, category)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void addAttachments(Long ticketId, List<MultipartFile> files, Long userId) {
        Ticket ticket = findById(ticketId);

        if (!ticket.getReporter().getId().equals(userId)) {
            throw new AccessDeniedException("Only the reporter can add attachments");
        }

        long existingCount = attachmentRepository.countByTicketId(ticketId);
        if (existingCount + files.size() > 3) {
            throw new BadRequestException("Maximum 3 attachments allowed per ticket");
        }

        Path uploadPath = Paths.get(uploadDir, "tickets", String.valueOf(ticketId));
        try {
            Files.createDirectories(uploadPath);
            for (MultipartFile file : files) {
                String ext = "";
                String orig = file.getOriginalFilename();
                if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf("."));
                String savedName = UUID.randomUUID() + ext;
                Path dest = uploadPath.resolve(savedName);
                Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

                TicketAttachment att = TicketAttachment.builder()
                        .ticket(ticket)
                        .fileName(orig)
                        .filePath(dest.toString())
                        .contentType(file.getContentType())
                        .fileSize(file.getSize())
                        .build();
                attachmentRepository.save(att);
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to save attachment: " + e.getMessage());
        }
    }

    @Override
    public CommentResponse addComment(Long ticketId, CommentRequest req, Long userId) {
        Ticket ticket = findById(ticketId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .author(author)
                .content(req.getContent())
                .build();
        Comment saved = commentRepository.save(comment);

        // Notify ticket reporter if commenter is not the reporter
        if (!ticket.getReporter().getId().equals(userId)) {
            notificationService.sendNotification(ticket.getReporter(), NotificationType.NEW_COMMENT,
                    author.getName() + " commented on your ticket: " + ticket.getTitle(), ticketId, "TICKET");
        }

        return toCommentResponse(saved);
    }

    @Override
    public CommentResponse editComment(Long commentId, CommentRequest req, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("You can only edit your own comments");
        }
        comment.setContent(req.getContent());
        comment.setEdited(true);
        return toCommentResponse(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!comment.getAuthor().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You cannot delete this comment");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long ticketId) {
        findById(ticketId); // ensure ticket exists
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(this::toCommentResponse).collect(Collectors.toList());
    }

    private Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }

    public TicketResponse toResponse(Ticket t) {
        List<AttachmentResponse> attachments = t.getAttachments().stream()
                .map(a -> AttachmentResponse.builder()
                        .id(a.getId())
                        .fileName(a.getFileName())
                        .contentType(a.getContentType())
                        .fileSize(a.getFileSize())
                        .uploadedAt(a.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        return TicketResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .category(t.getCategory())
                .priority(t.getPriority())
                .status(t.getStatus())
                .location(t.getLocation())
                .preferredContact(t.getPreferredContact())
                .reporterId(t.getReporter().getId())
                .reporterName(t.getReporter().getName())
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .assignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getName() : null)
                .resolutionNotes(t.getResolutionNotes())
                .rejectionReason(t.getRejectionReason())
                .resolvedAt(t.getResolvedAt())
                .attachments(attachments)
                .commentCount(t.getComments().size())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private CommentResponse toCommentResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .ticketId(c.getTicket().getId())
                .authorId(c.getAuthor().getId())
                .authorName(c.getAuthor().getName())
                .authorPicture(c.getAuthor().getPictureUrl())
                .content(c.getContent())
                .edited(c.isEdited())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
