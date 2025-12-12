package com.uptrix.uptrix_backend.entity;

import com.uptrix.uptrix_backend.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "code"}),
                @UniqueConstraint(columnNames = {"company_id", "name"})
        })
@Getter
@Setter
@ToString
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String code; // HR, ENG, SALES, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(length = 30)
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
}
