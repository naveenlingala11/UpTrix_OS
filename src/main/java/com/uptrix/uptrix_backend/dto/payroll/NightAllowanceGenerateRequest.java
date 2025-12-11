package com.uptrix.uptrix_backend.dto.payroll;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NightAllowanceGenerateRequest {

    private Long employeeId;
    private Integer year;
    private Integer month;

    /**
     * If true, overwrite existing NIGHT_ALLOWANCE earning for this month.
     */
    private Boolean overwriteExisting;
}
