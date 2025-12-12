package com.uptrix.uptrix_backend.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantFeaturesDto {

    private Long companyId;

    private boolean attendanceEnabled;
    private boolean leavesEnabled;
    private boolean shiftsEnabled;
    private boolean helpdeskEnabled;
    private boolean projectsEnabled;
    private boolean crmEnabled;

}
