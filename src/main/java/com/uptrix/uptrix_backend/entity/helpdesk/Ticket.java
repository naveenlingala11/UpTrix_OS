package com.uptrix.uptrix_backend.entity.helpdesk;

import com.uptrix.uptrix_backend.entity.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_owner_id")
    private Employee hrOwner;

    @ManyToOne
    @JoinColumn(name = "raised_by_employee_id")
    private Employee raisedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String priority;        // LOW / MEDIUM / HIGH / CRITICAL
    private String status;          // OPEN / IN_PROGRESS / RESOLVED / CLOSED
    private String visibilityScope; // EMPLOYEE_HR / EMPLOYEE_HR_MANAGER
    private String source;          // PORTAL / EMAIL etc.

    private LocalDateTime slaDueAt;
    private LocalDateTime closedAt;
    private Integer satisfactionScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters/setters...
}
