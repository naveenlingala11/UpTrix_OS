package com.uptrix.uptrix_backend.dto.attendance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoAttendancePunchRequest {

    private Long employeeId;

    /**
     * Optional – if null, system will try to resolve by
     * active shift assignment for today.
     */
    private Long shiftId;

    /**
     * IN or OUT
     */
    private String punchType;

    // GPS coordinates captured from device
    private Double latitude;
    private Double longitude;

    /**
     * Optional: MOBILE / WEB / OTHER
     */
    private String source;
}
