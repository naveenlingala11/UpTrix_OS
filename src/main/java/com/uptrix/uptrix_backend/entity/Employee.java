package com.uptrix.uptrix_backend.entity;

import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.company.CompanyLocation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "employee_code"}),
                @UniqueConstraint(columnNames = {"company_id", "work_email"})
        }
)
@Getter
@Setter
@ToString(exclude = {"company", "department", "location", "user", "manager"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, length = 50)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "work_email", length = 150)
    private String workEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private CompanyLocation location;

    // owning side of User–Employee link (employees.user_id)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "employment_type", length = 50)
    private String employmentType; // FULL_TIME, INTERN, CONTRACT

    @Column(name = "status", length = 30)
    private String status; // ACTIVE, EXITED, ON_HOLD

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Bank / beneficiary fields (optional, used by payroll bank CSV) ---
    @Column(name = "bank_account_number", length = 64)
    private String bankAccountNumber;

    @Column(name = "ifsc", length = 32)
    private String ifsc;

    @Column(name = "beneficiary_name", length = 200)
    private String beneficiaryName;

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

    // --- Convenience helpers so existing code calling getCode()/getFullName() works ---

    /**
     * Backwards-compatible: returns employeeCode
     */
    @Transient
    public String getCode() {
        return this.employeeCode;
    }

    /**
     * Backwards-compatible: full name composed from firstName + lastName.
     * Falls back to workEmail or "Employee-{id}" if names are missing.
     */
    @Transient
    public String getFullName() {
        String fn = (this.firstName != null ? this.firstName.trim() : "");
        String ln = (this.lastName != null ? this.lastName.trim() : "");
        String full = ((fn + " " + ln).trim());
        if (!full.isEmpty()) return full;
        if (this.workEmail != null && !this.workEmail.isBlank()) return this.workEmail;
        return this.id != null ? ("Employee-" + this.id) : null;
    }

    /**
     * Backwards-compatible aliases for bank fields (if other code calls these names).
     */
    @Transient
    public String getBankAccountNumberAlias() {
        return this.bankAccountNumber;
    }

    @Transient
    public String getIfscAlias() {
        return this.ifsc;
    }
}
