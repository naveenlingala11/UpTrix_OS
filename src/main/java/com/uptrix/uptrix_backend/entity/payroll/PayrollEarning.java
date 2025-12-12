package com.uptrix.uptrix_backend.entity.payroll;

import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payroll_earnings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "company_id", "employee_id", "year", "month", "component_code"
                })
        }
)
@Getter
@Setter
public class PayrollEarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month; // 1-12

    /**
     * e.g. NIGHT_ALLOWANCE, BASIC, HRA, etc.
     */
    @Column(name = "component_code", length = 100, nullable = false)
    private String componentCode;

    /**
     * Optional human-readable name (Night Allowance)
     */
    @Column(name = "component_name", length = 200)
    private String componentName;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency; // e.g. "INR"

    /**
     * GENERATED / ADJUSTED / FINALIZED
     */
    @Column(length = 30, nullable = false)
    private String status = "GENERATED";

    /**
     * TRUE once payroll is locked.
     */
    @Column(name = "locked", nullable = false)
    private Boolean locked = Boolean.FALSE;

    /**
     * Source of calculation, e.g. "NIGHT_ALLOWANCE_AUTO"
     */
    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.generatedAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = "GENERATED";
        }
        if (this.locked == null) {
            this.locked = Boolean.FALSE;
        }
        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "payroll_run_id")
    private PayrollRun payrollRun;

}
