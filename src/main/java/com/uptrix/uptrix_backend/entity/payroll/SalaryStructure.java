package com.uptrix.uptrix_backend.entity.payroll;

import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salary_structure")
@Getter
@Setter
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(length = 3)
    private String currency; // e.g. "INR"

    @Column(length = 20)
    private String status;   // ACTIVE / INACTIVE / DRAFT

    @OneToMany(mappedBy = "salaryStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalaryStructureComponent> components = new ArrayList<>();
}
