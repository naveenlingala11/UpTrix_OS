package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollEarningDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    private Integer year;
    private Integer month;

    private String componentCode;
    private String componentName;

    private BigDecimal amount;
    private String currency;
    private String status;
    private Boolean locked;

    private String source;
}
