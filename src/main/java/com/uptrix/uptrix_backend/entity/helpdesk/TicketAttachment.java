package com.uptrix.uptrix_backend.entity.helpdesk;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "ticket_attachments")
public class TicketAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    private String originalName;
    private String contentType;
    private Long fileSize;

    private String storagePath;   // e.g. /uploads/companies/{companyId}/tickets/{ticketId}/xxxx
    private LocalDateTime uploadedAt;
}
