package com.smartcampus.service;

import com.smartcampus.dto.request.CommentRequest;
import com.smartcampus.dto.request.TicketRequest;
import com.smartcampus.dto.request.TicketStatusRequest;
import com.smartcampus.dto.response.CommentResponse;
import com.smartcampus.dto.response.TicketResponse;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketRequest request, Long userId);
    TicketResponse updateTicketStatus(Long ticketId, TicketStatusRequest request, Long requesterId);
    TicketResponse getTicketById(Long ticketId);
    List<TicketResponse> getMyTickets(Long userId, TicketStatus status);
    List<TicketResponse> getAllTickets(TicketStatus status, TicketPriority priority, String category);
    void addAttachments(Long ticketId, List<MultipartFile> files, Long userId);
    CommentResponse addComment(Long ticketId, CommentRequest request, Long userId);
    CommentResponse editComment(Long commentId, CommentRequest request, Long userId);
    void deleteComment(Long commentId, Long userId);
    List<CommentResponse> getComments(Long ticketId);
}
