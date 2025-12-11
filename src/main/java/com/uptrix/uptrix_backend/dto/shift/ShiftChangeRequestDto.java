package com.uptrix.uptrix_backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftChangeRequestDto {

    private Long employeeId;
    private Long fromShiftId;
    private Long toShiftId;
    private String effectiveFrom;   // "YYYY-MM-DD"
    private String effectiveTo;     // optional
    private String reason;
}
