package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MyPayslipDto {

    private Long payrollRunId;
    private Integer year;
    private Integer month;
    private String runType;
    private String status;

    private BigDecimal grossEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
}
