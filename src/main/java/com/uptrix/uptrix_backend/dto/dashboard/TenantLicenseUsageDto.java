package com.uptrix.uptrix_backend.dto.dashboard;

public class TenantLicenseUsageDto {

    private Long companyId;
    private String companyName;
    private Integer seatLimit;
    private long usedSeats;
    private int utilisationPercent;

    public TenantLicenseUsageDto() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getSeatLimit() {
        return seatLimit;
    }

    public void setSeatLimit(Integer seatLimit) {
        this.seatLimit = seatLimit;
    }

    public long getUsedSeats() {
        return usedSeats;
    }

    public void setUsedSeats(long usedSeats) {
        this.usedSeats = usedSeats;
    }

    public int getUtilisationPercent() {
        return utilisationPercent;
    }

    public void setUtilisationPercent(int utilisationPercent) {
        this.utilisationPercent = utilisationPercent;
    }
}
