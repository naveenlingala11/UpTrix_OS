package com.uptrix.uptrix_backend.service.shift;

import com.uptrix.uptrix_backend.dto.shift.ShiftRequest;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Transactional(readOnly = true)
    public List<Shift> getAllActive() {
        return shiftRepository.findByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<Shift> getAll() {
        return shiftRepository.findAll();
    }

    @Transactional
    public Shift create(ShiftRequest req) {
        validateShiftRequest(req);

        if (shiftRepository.existsByCode(req.getCode())) {
            throw new IllegalArgumentException("Shift code already exists: " + req.getCode());
        }

        Shift shift = new Shift();
        applyRequestToEntity(req, shift);

        return shiftRepository.save(shift);
    }

    @Transactional
    public Shift updateStatus(Long id, String status) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));

        String normalized = status == null ? null : status.trim().toUpperCase();
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new IllegalArgumentException("Invalid status. Allowed: ACTIVE / INACTIVE");
        }

        shift.setStatus(normalized);
        return shiftRepository.save(shift);
    }

    private void applyRequestToEntity(ShiftRequest req, Shift shift) {
        shift.setName(req.getName().trim());
        shift.setCode(req.getCode().trim());
        shift.setStartTime(LocalTime.parse(req.getStartTime()));
        shift.setEndTime(LocalTime.parse(req.getEndTime()));
        shift.setGraceMinutes(req.getGraceMinutes());

        Boolean night = req.getNightShift();
        shift.setNightShift(night != null ? night : Boolean.FALSE);

        Boolean autoAllowance = req.getAutoNightAllowance();
        shift.setAutoNightAllowance(autoAllowance != null ? autoAllowance : Boolean.FALSE);

        if (req.getNightAllowanceAmount() != null) {
            BigDecimal amt = req.getNightAllowanceAmount();
            if (amt.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Night allowance cannot be negative");
            }
            shift.setNightAllowanceAmount(amt);
        } else {
            shift.setNightAllowanceAmount(null);
        }

        shift.setRotationGroup(req.getRotationGroup());
        shift.setRotationOrder(req.getRotationOrder());

        shift.setGeoLatitude(req.getGeoLatitude());
        shift.setGeoLongitude(req.getGeoLongitude());
        shift.setGeoRadiusMeters(req.getGeoRadiusMeters());
    }

    /**
     * Simple policy validation: ensure proper times etc.
     */
    private void validateShiftRequest(ShiftRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Invalid request");
        }
        if (!StringUtils.hasText(req.getName())) {
            throw new IllegalArgumentException("Shift name is required");
        }
        if (!StringUtils.hasText(req.getCode())) {
            throw new IllegalArgumentException("Shift code is required");
        }
        if (!StringUtils.hasText(req.getStartTime()) || !StringUtils.hasText(req.getEndTime())) {
            throw new IllegalArgumentException("Start and end time are required");
        }

        LocalTime start = LocalTime.parse(req.getStartTime());
        LocalTime end = LocalTime.parse(req.getEndTime());

        // Example rule: shift must be at least 1 hour and not more than 16 hours
        long minutes = java.time.Duration.between(start, end).toMinutes();
        if (minutes <= 0) {
            // Allow overnight if nightShift = true
            if (Boolean.TRUE.equals(req.getNightShift())) {
                // ok – overnight shift like 22:00 -> 06:00
            } else {
                throw new IllegalArgumentException("End time must be after start time for non-night shifts");
            }
        } else if (minutes < 60) {
            throw new IllegalArgumentException("Shift duration must be at least 1 hour");
        } else if (minutes > 16 * 60) {
            throw new IllegalArgumentException("Shift duration cannot exceed 16 hours");
        }
    }
}
