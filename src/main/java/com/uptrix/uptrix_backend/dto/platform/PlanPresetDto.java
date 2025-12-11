package com.uptrix.uptrix_backend.dto.platform;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanPresetDto {
    private long id;
    private String name;
    private String description;
    private int maxEmployees;
    private List<String> modules;
    private boolean recommended;
}
