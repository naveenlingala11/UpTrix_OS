package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NightAllowancePreviewRequest {

    private Long employeeId;
    private Integer year;   // e.g. 2025
    private Integer month;  // 1-12
}
