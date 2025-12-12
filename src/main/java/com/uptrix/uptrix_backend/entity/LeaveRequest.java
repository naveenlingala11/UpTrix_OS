package com.uptrix.uptrix_backend.entity;

import com.uptrix.uptrix_backend.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company to which this leave belongs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Employee who requested leave
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Leave period
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Leave type
    @Column(name = "leave_type", length = 50)
    private String leaveType;

    // Reason from employee
    @Column(name = "reason", length = 500)
    private String reason;

    // Status (PENDING, APPROVED, REJECTED)
    @Column(name = "status", length = 30)
    private String status;

    // When request was created
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // When request was last updated
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Who approved / rejected this leave
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_user_id")
    private User decidedBy;

    // When it was approved / rejected
    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    // Comment by HR / Manager while approving/rejecting
    @Column(name = "decision_comment", length = 500)
    private String decisionComment;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = this.status == null ? "PENDING" : this.status;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
