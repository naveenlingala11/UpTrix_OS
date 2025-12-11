package com.uptrix.uptrix_backend.dto.platform;

import com.uptrix.uptrix_backend.entity.platform.enums.EnvironmentStatus;
import lombok.Data;

@Data
public class EnvironmentPresetDto {
    private Long id;
    private String name;
    private String key;
    private EnvironmentStatus status;
    private String region;
}
