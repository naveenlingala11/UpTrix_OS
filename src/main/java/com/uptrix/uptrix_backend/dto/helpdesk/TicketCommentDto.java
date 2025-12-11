package com.uptrix.uptrix_backend.dto.helpdesk;

import java.time.LocalDateTime;

public class TicketCommentDto {

    private Long id;
    private String authorName;
    private String authorRole;
    private boolean internal;
    private String message;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }
    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public boolean isInternal() {
        return internal;
    }
    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
