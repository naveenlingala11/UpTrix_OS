package com.uptrix.uptrix_backend.service.attendance;

import com.uptrix.uptrix_backend.dto.attendance.GeoAttendancePunchRequest;
import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.EmployeeShiftAssignment;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.repository.AttendanceRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.ShiftRepository;
import com.uptrix.uptrix_backend.service.shift.EmployeeShiftService;
import com.uptrix.uptrix_backend.util.GeoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class GeoAttendanceService {

    private static final double DEFAULT_ALLOWED_RADIUS_METERS = 200.0;

    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeShiftService employeeShiftService;
    private final AttendanceRepository attendanceRepository;

    public GeoAttendanceService(EmployeeRepository employeeRepository,
                                ShiftRepository shiftRepository,
                                EmployeeShiftService employeeShiftService,
                                AttendanceRepository attendanceRepository) {
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.employeeShiftService = employeeShiftService;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public Attendance punch(GeoAttendancePunchRequest req) {
        if (req == null || req.getEmployeeId() == null) {
            throw new IllegalArgumentException("employeeId is required");
        }
        if (!StringUtils.hasText(req.getPunchType())) {
            throw new IllegalArgumentException("punchType (IN / OUT) is required");
        }

        String punchType = req.getPunchType().trim().toUpperCase();
        if (!"IN".equals(punchType) && !"OUT".equals(punchType)) {
            throw new IllegalArgumentException("punchType must be IN or OUT");
        }

        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate today = LocalDate.now();

        // Try to get existing attendance row for today, or create new
        Attendance attendance = attendanceRepository
                .findByEmployeeAndDate(employee, today)
                .orElseGet(() -> {
                    Attendance a = new Attendance();
                    a.setEmployee(employee);
                    a.setCompany(employee.getCompany()); // assuming Employee has getCompany()
                    a.setDate(today);
                    // status and date defaults are handled by @PrePersist
                    return a;
                });

        // ----- GEO-FENCE CALCULATION -----
        Shift shift = null;

        if (req.getShiftId() != null) {
            shift = shiftRepository.findById(req.getShiftId())
                    .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
        } else {
            EmployeeShiftAssignment assignment =
                    employeeShiftService.getActiveAssignmentForEmployeeOnDate(employee.getId(), today);
            if (assignment != null) {
                shift = assignment.getShift();
            }
        }

        Double geoLat = req.getLatitude();
        Double geoLon = req.getLongitude();

        Double distanceMeters = null;
        Boolean withinRadius = null;

        if (geoLat != null && geoLon != null && shift != null &&
                shift.getGeoLatitude() != null && shift.getGeoLongitude() != null) {

            distanceMeters = GeoUtils.distanceInMeters(
                    geoLat,
                    geoLon,
                    shift.getGeoLatitude(),
                    shift.getGeoLongitude()
            );

            int allowedRadius = (shift.getGeoRadiusMeters() != null && shift.getGeoRadiusMeters() > 0)
                    ? shift.getGeoRadiusMeters()
                    : (int) DEFAULT_ALLOWED_RADIUS_METERS;

            withinRadius = distanceMeters <= allowedRadius;
        }

        attendance.setGeoLatitude(geoLat);
        attendance.setGeoLongitude(geoLon);
        attendance.setGeoDistanceMeters(distanceMeters);
        attendance.setGeoWithinRadius(withinRadius);

        // ----- CHECK-IN / CHECK-OUT LOGIC -----
        LocalDateTime now = LocalDateTime.now();

        if ("IN".equals(punchType)) {
            if (attendance.getCheckInTime() == null) {
                attendance.setCheckInTime(now);
            } else {
                // Optional: throw error instead of overriding
                // throw new IllegalStateException("Check-in already recorded for today");
                attendance.setCheckInTime(now); // overwrite last IN
            }
        } else { // OUT
            if (attendance.getCheckOutTime() == null) {
                attendance.setCheckOutTime(now);
            } else {
                // Optional: same as above
                attendance.setCheckOutTime(now);
            }
        }

        // You can optionally tweak status here based on IN/OUT/geoWithinRadius
        // e.g. mark HALF_DAY if only one punch by end of day, etc.

        return attendanceRepository.save(attendance);
    }
}
