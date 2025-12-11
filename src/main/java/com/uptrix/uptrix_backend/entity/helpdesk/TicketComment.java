package com.uptrix.uptrix_backend.entity.helpdesk;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_comments")
@Getter
@Setter
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_role", nullable = false)
    private String authorRole;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "internal_only", nullable = false)
    private boolean internal = false;   // ✅ DEFAULT FIX

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
