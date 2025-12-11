package com.uptrix.uptrix_backend.dto.leave;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class LeaveRequestDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    // Decision info
    private String decidedByName;
    private LocalDateTime decidedAt;
    private String decisionComment;
}
