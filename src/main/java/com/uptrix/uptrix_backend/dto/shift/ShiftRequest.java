package com.uptrix.uptrix_backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ShiftRequest {

    private String name;
    private String code;
    private String startTime;           // "09:00"
    private String endTime;             // "18:00"
    private Integer graceMinutes;
    private Boolean nightShift;

    // Night shift auto-allowance flags
    private Boolean autoNightAllowance;
    private BigDecimal nightAllowanceAmount;

    // Rotational shift support
    private String rotationGroup;       // e.g. "WEEKLY_ROTATION_A"
    private Integer rotationOrder;      // e.g. 1,2,3...

    // Geo-fence support
    private Double geoLatitude;
    private Double geoLongitude;
    private Integer geoRadiusMeters;
}
