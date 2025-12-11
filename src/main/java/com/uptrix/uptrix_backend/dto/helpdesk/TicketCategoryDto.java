package com.uptrix.uptrix_backend.dto.helpdesk;

public class TicketCategoryDto {

    private Long id;
    private String name;
    private String description;
    private Integer defaultSlaHours;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDefaultSlaHours() {
        return defaultSlaHours;
    }
    public void setDefaultSlaHours(Integer defaultSlaHours) {
        this.defaultSlaHours = defaultSlaHours;
    }
}
