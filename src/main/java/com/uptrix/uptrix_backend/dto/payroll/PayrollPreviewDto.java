package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PayrollPreviewDto {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private Integer year;
    private Integer month;

    private BigDecimal grossEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;

    private List<PayrollEarningDto> earnings;
    private List<PayrollDeductionDto> deductions;
}
