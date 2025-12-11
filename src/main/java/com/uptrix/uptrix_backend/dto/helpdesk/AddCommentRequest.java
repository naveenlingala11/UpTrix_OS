package com.uptrix.uptrix_backend.dto.helpdesk;

public class AddCommentRequest {

    private String message;
    private Boolean internal;

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getInternal() {
        return internal;
    }
    public void setInternal(Boolean internal) {
        this.internal = internal;
    }
}
