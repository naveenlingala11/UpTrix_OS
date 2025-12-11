package com.uptrix.uptrix_backend.service.attendance;

import com.uptrix.uptrix_backend.dto.attendance.AttendanceDayDto;
import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.repository.AttendanceRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceQueryService(AttendanceRepository attendanceRepository,
                                  EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<AttendanceDayDto> getMonthForEmployee(Long employeeId, int year, int month) {
        if (employeeId == null) {
            throw new IllegalArgumentException("employeeId is required");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Attendance> list = attendanceRepository
                .findByEmployeeAndDateBetween(employee, start, end);

        return list.stream()
                .map(a -> {
                    AttendanceDayDto dto = new AttendanceDayDto();
                    dto.setDate(a.getDate());
                    dto.setCheckInTime(a.getCheckInTime());
                    dto.setCheckOutTime(a.getCheckOutTime());
                    dto.setStatus(a.getStatus());
                    if (a.getCheckInTime() != null && a.getCheckOutTime() != null) {
                        long minutes = Duration.between(
                                a.getCheckInTime(),
                                a.getCheckOutTime()
                        ).toMinutes();
                        dto.setWorkedMinutes(minutes);
                    } else {
                        dto.setWorkedMinutes(null);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
