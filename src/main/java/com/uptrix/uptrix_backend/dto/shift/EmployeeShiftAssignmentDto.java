package com.uptrix.uptrix_backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeShiftAssignmentDto {

    private Long id;

    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private ShiftSummaryDto shift;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    private Boolean exceptionOverride;
}
