package com.uptrix.uptrix_backend.dto.shift;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShiftGeoUpdateRequest {

    private Double latitude;
    private Double longitude;
    private Integer radiusMeters;
}
