package com.uptrix.uptrix_backend.entity.payroll;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salary_structure_component")
@Getter
@Setter
public class SalaryStructureComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "salary_structure_id")
    private SalaryStructure salaryStructure;

    @Column(nullable = false, length = 50)
    private String componentCode;   // BASIC, HRA, PF, ESI, TDS, OT, etc.

    @Column(length = 100)
    private String componentName;

    @Column(length = 20)
    private String componentType;   // EARNING / DEDUCTION

    @Column(length = 20)
    private String calculationType; // FIXED / PERCENT_OF_BASIC / FORMULA

    @Column(name = "amount_value")
    private Double amountValue;     // used if FIXED

    @Column(name = "percent_of_basic")
    private Double percentOfBasic;  // used if PERCENT_OF_BASIC

    @Column(name = "is_taxable")
    private Boolean taxable;

    @Column(name = "sequence_no")
    private Integer sequenceNo;
}
