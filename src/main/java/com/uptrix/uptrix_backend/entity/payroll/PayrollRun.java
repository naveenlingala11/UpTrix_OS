package com.uptrix.uptrix_backend.entity.payroll;

import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_runs")
@Getter
@Setter
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month; // 1-12

    @Column(length = 20)
    private String runType; // REGULAR / BONUS / ADHOC

    @Column(length = 20)
    private String status; // DRAFT / APPROVED / LOCKED

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollDeduction> deductions = new ArrayList<>();

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollIncentive> incentives = new ArrayList<>();
}
