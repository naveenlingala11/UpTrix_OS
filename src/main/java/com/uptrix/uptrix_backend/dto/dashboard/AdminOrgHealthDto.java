package com.uptrix.uptrix_backend.dto.dashboard;

public class AdminOrgHealthDto {

    private Long companyId;

    private long totalEmployees;
    private long totalDepartments;

    private double avgEmployeesPerDepartment;

    public AdminOrgHealthDto() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public double getAvgEmployeesPerDepartment() {
        return avgEmployeesPerDepartment;
    }

    public void setAvgEmployeesPerDepartment(double avgEmployeesPerDepartment) {
        this.avgEmployeesPerDepartment = avgEmployeesPerDepartment;
    }
}
