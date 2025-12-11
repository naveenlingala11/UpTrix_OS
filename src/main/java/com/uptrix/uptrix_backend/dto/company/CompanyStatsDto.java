package com.uptrix.uptrix_backend.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyStatsDto {

    private Long companyId;
    private String companyName;

    private long totalUsers;
    private long totalEmployees;
    private long totalDepartments;
    private long totalEmployeesPresentToday;

    private long pendingLeaves;

    private long onLeaveToday;
    private long unplannedAbsentToday;
    private long employeesOnProbation;
    private long upcomingJoiners;
    private long newJoineesLast30;
    private long exitedLast30;
    private long totalShifts;
    private long employeesWithoutShift;
    private long nightShiftHeadcount;

}
