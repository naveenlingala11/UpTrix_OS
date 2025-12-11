package com.uptrix.uptrix_backend.controller.helpdesk;

import com.uptrix.uptrix_backend.dto.UserPrincipal;
import com.uptrix.uptrix_backend.dto.helpdesk.*;
import com.uptrix.uptrix_backend.service.helpdesk.HelpdeskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/helpdesk")
public class HelpdeskController {

    private final HelpdeskService helpdeskService;

    public HelpdeskController(HelpdeskService helpdeskService) {
        this.helpdeskService = helpdeskService;
    }

    @Operation(summary = "List ticket categories", description = "Get active helpdesk categories for company",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/categories")
    public ResponseEntity<List<TicketCategoryDto>> listCategories(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(helpdeskService.getActiveCategories(companyId));
    }

    @Operation(summary = "Create a ticket", description = "Create a new helpdesk ticket (employee resolved from JWT)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tickets")
    public ResponseEntity<TicketDetailDto> createTicket(
            @PathVariable Long companyId,
            @RequestBody CreateTicketRequest req
    ) {
        return ResponseEntity.ok(
                helpdeskService.createTicket(companyId, req)
        );
    }

    @Operation(summary = "Get my tickets (paged)", description = "Returns tickets for logged-in user depending on role",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tickets/my")
    public ResponseEntity<Page<TicketSummaryDto>> getMyTickets(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                helpdeskService.getMyTickets(companyId, page, size)
        );
    }

    @Operation(summary = "List all tickets (HR/Admin)", description = "Returns tickets for HR/Admin views",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tickets")
    public ResponseEntity<Page<TicketSummaryDto>> allTicketsForHr(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                helpdeskService.listTicketsForHr(companyId, null, page, size)
        );
    }

    @Operation(summary = "Get ticket detail", description = "Get detailed ticket by id",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketDetailDto> getTicket(
            @PathVariable Long companyId,
            @PathVariable Long ticketId
    ) {
        return ResponseEntity.ok(
                helpdeskService.getTicket(companyId, ticketId)
        );
    }

    @Operation(summary = "Add comment to ticket", description = "Add a comment to ticket",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<TicketDetailDto> addComment(
            @PathVariable Long companyId,
            @PathVariable Long ticketId,
            @RequestBody AddCommentRequest req
    ) {
        return ResponseEntity.ok(
                helpdeskService.addComment(companyId, ticketId, req)
        );
    }

    @Operation(summary = "Update ticket", description = "Partial update (status/owner/priority)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketDetailDto> updateTicket(
            @PathVariable Long companyId,
            @PathVariable Long ticketId,
            @RequestBody UpdateTicketRequest req
    ) {
        return ResponseEntity.ok(
                helpdeskService.updateTicket(companyId, ticketId, req)
        );
    }

    @Operation(summary = "Set ticket satisfaction score", description = "Set satisfaction for ticket",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tickets/{ticketId}/satisfaction")
    public ResponseEntity<TicketDetailDto> setSatisfaction(
            @PathVariable Long companyId,
            @PathVariable Long ticketId,
            @RequestBody SatisfactionRequest req
    ) {
        return ResponseEntity.ok(
                helpdeskService.setSatisfaction(companyId, ticketId, req.getScore())
        );
    }

    @Operation(summary = "Upload attachments for ticket", description = "Upload files for a ticket",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/tickets/{ticketId}/attachments", consumes = "multipart/form-data")
    public ResponseEntity<List<TicketAttachmentDto>> uploadAttachments(
            @PathVariable Long companyId,
            @PathVariable Long ticketId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(
                helpdeskService.uploadAttachments(companyId, ticketId, files)
        );
    }

    @Operation(summary = "Download ticket attachment", description = "Download a specific attachment",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tickets/{ticketId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long companyId,
            @PathVariable Long ticketId,
            @PathVariable Long attachmentId
    ) {
        return helpdeskService.downloadAttachment(companyId, ticketId, attachmentId);
    }
}
