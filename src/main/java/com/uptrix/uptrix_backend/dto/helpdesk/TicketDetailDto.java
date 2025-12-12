package com.uptrix.uptrix_backend.dto.helpdesk;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDetailDto {

    private Long id;
    private String subject;
    private String description;
    private String status;
    private String priority;

    private Long categoryId;
    private String categoryName;

    private Long employeeId;
    private String employeeName;

    private Long hrOwnerId;
    private String hrOwnerName;

    private String source;
    private String visibilityScope;

    private LocalDateTime slaDueAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer satisfactionScore;

    private List<TicketCommentDto> comments;
    private List<TicketAttachmentDto> attachments;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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

    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getHrOwnerId() {
        return hrOwnerId;
    }
    public void setHrOwnerId(Long hrOwnerId) {
        this.hrOwnerId = hrOwnerId;
    }

    public String getHrOwnerName() {
        return hrOwnerName;
    }
    public void setHrOwnerName(String hrOwnerName) {
        this.hrOwnerName = hrOwnerName;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public String getVisibilityScope() {
        return visibilityScope;
    }
    public void setVisibilityScope(String visibilityScope) {
        this.visibilityScope = visibilityScope;
    }

    public LocalDateTime getSlaDueAt() {
        return slaDueAt;
    }
    public void setSlaDueAt(LocalDateTime slaDueAt) {
        this.slaDueAt = slaDueAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getSatisfactionScore() {
        return satisfactionScore;
    }
    public void setSatisfactionScore(Integer satisfactionScore) {
        this.satisfactionScore = satisfactionScore;
    }

    public List<TicketCommentDto> getComments() {
        return comments;
    }
    public void setComments(List<TicketCommentDto> comments) {
        this.comments = comments;
    }

    public List<TicketAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<TicketAttachmentDto> attachments) {
        this.attachments = attachments;
    }
}
