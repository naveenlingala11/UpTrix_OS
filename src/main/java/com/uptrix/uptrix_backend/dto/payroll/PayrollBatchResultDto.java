package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PayrollBatchResultDto {

    private Long companyId;
    private Integer year;
    private Integer month;
    private String runType;

    private Long payrollRunId;
    private Integer processedCount;

    private List<EmployeePayrollResultDto> results;
}
