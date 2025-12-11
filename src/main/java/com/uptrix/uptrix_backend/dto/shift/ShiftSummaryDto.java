package com.uptrix.uptrix_backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ShiftSummaryDto {

    private Long id;
    private String code;
    private String name;

    // Use LocalTime here to match entity type
    private LocalTime startTime;  // e.g. 09:00
    private LocalTime endTime;    // e.g. 18:00

    private Boolean nightShift;

    private Double geoLatitude;
    private Double geoLongitude;
    private Integer geoRadiusMeters;
}
