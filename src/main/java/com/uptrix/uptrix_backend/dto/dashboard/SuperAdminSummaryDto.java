package com.uptrix.uptrix_backend.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminSummaryDto {

    private long totalCompanies;
    private long totalUsers;
    private long activeTenants;
}
