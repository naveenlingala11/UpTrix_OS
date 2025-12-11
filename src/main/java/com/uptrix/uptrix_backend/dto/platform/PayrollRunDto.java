package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PayrollRunDto {
    private Long id;
    private Long companyId;
    private Integer year;
    private Integer month;
    private String runType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
