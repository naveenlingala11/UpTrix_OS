package com.uptrix.uptrix_backend.dto.helpdesk;

public class UpdateTicketRequest {

    private String status;        // e.g. OPEN / IN_PROGRESS / RESOLVED / CLOSED
    private String priority;      // LOW / MEDIUM / HIGH / CRITICAL
    private Long hrOwnerId;       // assign / reassign HR owner (Employee.id)

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getHrOwnerId() {
        return hrOwnerId;
    }
    public void setHrOwnerId(Long hrOwnerId) {
        this.hrOwnerId = hrOwnerId;
    }
}
