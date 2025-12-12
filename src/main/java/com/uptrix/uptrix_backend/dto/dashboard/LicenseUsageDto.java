package com.uptrix.uptrix_backend.dto.dashboard;

import java.util.List;

public class LicenseUsageDto {

    private long totalSeatLimit;
    private long totalUsedSeats;
    private List<TenantLicenseUsageDto> tenants;

    public LicenseUsageDto() {
    }

    public long getTotalSeatLimit() {
        return totalSeatLimit;
    }

    public void setTotalSeatLimit(long totalSeatLimit) {
        this.totalSeatLimit = totalSeatLimit;
    }

    public long getTotalUsedSeats() {
        return totalUsedSeats;
    }

    public void setTotalUsedSeats(long totalUsedSeats) {
        this.totalUsedSeats = totalUsedSeats;
    }

    public List<TenantLicenseUsageDto> getTenants() {
        return tenants;
    }

    public void setTenants(List<TenantLicenseUsageDto> tenants) {
        this.tenants = tenants;
    }
}
