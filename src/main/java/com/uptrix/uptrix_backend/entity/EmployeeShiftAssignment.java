package com.uptrix.uptrix_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(
        name = "employee_shift_assignments"
)
public class EmployeeShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee to whom this shift is assigned.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Assigned shift.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    /**
     * Start date (inclusive) of this assignment.
     */
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    /**
     * End date (inclusive). Null = open ended.
     */
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    /**
     * If TRUE, this record is an exception/override for specific dates
     * (e.g. shift swap, temporary change).
     */
    @Column(name = "is_exception", nullable = false)
    private Boolean exceptionOverride = Boolean.FALSE;

    /**
     * Optional rotation sequence index for analytics / reporting.
     */
    @Column(name = "rotation_sequence")
    private Integer rotationSequence;

    @Column(length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.exceptionOverride == null) {
            this.exceptionOverride = Boolean.FALSE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
