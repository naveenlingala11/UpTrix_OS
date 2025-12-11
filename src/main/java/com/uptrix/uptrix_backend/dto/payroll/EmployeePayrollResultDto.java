package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeePayrollResultDto {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private boolean success;
    private String errorMessage;
}
