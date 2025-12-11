package com.uptrix.uptrix_backend.dto.dashboard;

public class PlatformAlertDto {

    private String type;        // INFO / WARN / CRITICAL
    private String title;
    private String description;
    private String scope;       // tenant name or "Platform"

    public PlatformAlertDto() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
