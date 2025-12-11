package com.uptrix.uptrix_backend.service.shift;

import com.uptrix.uptrix_backend.dto.shift.ShiftGeoUpdateRequest;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShiftGeoService {

    private final ShiftRepository shiftRepository;

    public ShiftGeoService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Transactional
    public Shift updateGeo(Long shiftId, ShiftGeoUpdateRequest req) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));

        if (req.getLatitude() == null || req.getLongitude() == null) {
            // Allow clearing geofence
            shift.setGeoLatitude(null);
            shift.setGeoLongitude(null);
            shift.setGeoRadiusMeters(null);
        } else {
            shift.setGeoLatitude(req.getLatitude());
            shift.setGeoLongitude(req.getLongitude());
            shift.setGeoRadiusMeters(req.getRadiusMeters() != null && req.getRadiusMeters() > 0
                    ? req.getRadiusMeters()
                    : 200); // default 200m
        }

        return shiftRepository.save(shift);
    }
}
