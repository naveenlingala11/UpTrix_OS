package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SalaryStructureDto {

    private Long id;
    private Long employeeId;
    private Long companyId;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String currency;
    private String status;

    private List<SalaryStructureComponentDto> components;
}
