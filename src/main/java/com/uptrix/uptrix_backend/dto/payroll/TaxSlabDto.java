package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaxSlabDto {

    private Long id;
    private Long companyId;

    private BigDecimal fromAmount;
    private BigDecimal toAmount;

    private Double ratePercent;
    private BigDecimal fixedAmount;

    private Integer sortOrder;
    private Boolean active;
}
