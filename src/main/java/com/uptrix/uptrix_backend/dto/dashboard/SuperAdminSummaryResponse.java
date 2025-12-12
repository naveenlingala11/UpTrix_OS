package com.uptrix.uptrix_backend.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminSummaryResponse {
    private int totalCompanies;
    private int totalUsers;
    private int activeTenants;
}
