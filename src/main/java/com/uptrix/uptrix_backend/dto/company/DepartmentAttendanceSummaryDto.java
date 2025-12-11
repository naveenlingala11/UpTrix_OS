package com.uptrix.uptrix_backend.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentAttendanceSummaryDto {

    private Long departmentId;         // null = no department
    private String departmentName;

    private long totalEmployees;
    private long presentCount;
    private long onLeaveCount;
    private long unplannedAbsentCount;
}
