package com.uptrix.uptrix_backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeShiftAssignRequest {

    private Long employeeId;
    private Long shiftId;
    private String effectiveFrom;    // "2025-01-01"
    private String effectiveTo;      // optional, can be null

    /**
     * If true, this assignment is treated as an exception override
     * (e.g. approved shift change for specific dates).
     */
    private Boolean exceptionOverride;

    /**
     * Optional rotation sequence index (for reporting).
     */
    private Integer rotationSequence;
}
