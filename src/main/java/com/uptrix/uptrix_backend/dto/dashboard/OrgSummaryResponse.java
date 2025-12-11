package com.uptrix.uptrix_backend.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgSummaryResponse {

    // For tenant admin, mostly 1 company (their own)
    private int companies;

    // Users with login in this company
    private int activeUsers;

    // License utilisation % (simple calc / placeholder)
    private int licenseUtilization;
}
