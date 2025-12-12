package com.uptrix.uptrix_backend.dto.leave;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequestCreateDto {

    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private String reason;
}
