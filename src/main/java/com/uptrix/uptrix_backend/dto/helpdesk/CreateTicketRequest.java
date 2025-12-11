package com.uptrix.uptrix_backend.dto.helpdesk;

public class CreateTicketRequest {

    private Long employeeId;   // who is raising the ticket
    private Long categoryId;
    private String subject;
    private String description;
    private String priority;   // LOW / MEDIUM / HIGH / CRITICAL

    public Long getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
}
