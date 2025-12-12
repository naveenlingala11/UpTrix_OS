package com.uptrix.uptrix_backend.dto.company;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RecentLeaveDto {

    private Long id;
    private String employeeName;
    private String departmentName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
