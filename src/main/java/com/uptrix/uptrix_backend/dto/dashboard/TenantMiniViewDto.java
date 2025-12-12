package com.uptrix.uptrix_backend.dto.dashboard;

public class TenantMiniViewDto {

    private Long id;
    private String companyName;
    private String subdomain;
    private String status;      // e.g. TRIAL / LIVE / PAUSED
    private long users;
    private long admins;
    private String lastActive;  // human label, e.g. "2h ago"

    public TenantMiniViewDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }

    public long getAdmins() {
        return admins;
    }

    public void setAdmins(long admins) {
        this.admins = admins;
    }

    public String getLastActive() {
        return lastActive;
    }

    public void setLastActive(String lastActive) {
        this.lastActive = lastActive;
    }
}
