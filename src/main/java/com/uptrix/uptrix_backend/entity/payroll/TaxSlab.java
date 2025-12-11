package com.uptrix.uptrix_backend.entity.payroll;

import com.uptrix.uptrix_backend.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_slab")
@Getter
@Setter
public class TaxSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    /**
     * Lower bound (inclusive) of annual taxable income for this slab.
     */
    @Column(name = "from_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal fromAmount;

    /**
     * Upper bound (exclusive) of annual taxable income for this slab.
     * NULL means "no upper limit".
     */
    @Column(name = "to_amount", precision = 15, scale = 2)
    private BigDecimal toAmount;

    /**
     * Percentage rate to apply on the slab band, e.g. 5, 20, 30.
     */
    @Column(name = "rate_percent", nullable = false)
    private Double ratePercent;

    /**
     * Optional fixed amount for this slab (for advanced use).
     */
    @Column(name = "fixed_amount", precision = 15, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active")
    private Boolean active = Boolean.TRUE;
}
