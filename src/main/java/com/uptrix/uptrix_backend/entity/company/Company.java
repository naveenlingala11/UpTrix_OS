package com.uptrix.uptrix_backend.entity.company;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
@ToString
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "subdomain", unique = true, length = 100)
    private String subdomain;

    @Column(name = "status", length = 30)
    private String status; // ACTIVE, INACTIVE

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "plan_code")
    private String planCode;

    @Column(name = "seat_limit")
    private Integer seatLimit;

}
