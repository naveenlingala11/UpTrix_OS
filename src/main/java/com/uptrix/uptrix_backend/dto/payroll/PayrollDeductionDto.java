package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollDeductionDto {
    private Long id;
    private String code;
    private String name;
    private BigDecimal amount;
    private String currency;
}
