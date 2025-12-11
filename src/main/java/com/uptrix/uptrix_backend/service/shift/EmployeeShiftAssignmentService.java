package com.uptrix.uptrix_backend.service.shift;

import com.uptrix.uptrix_backend.dto.shift.EmployeeShiftAssignmentDto;
import com.uptrix.uptrix_backend.dto.shift.ShiftSummaryDto;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.EmployeeShiftAssignment;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.EmployeeShiftAssignmentRepository;
import com.uptrix.uptrix_backend.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeShiftAssignmentService {

    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    public EmployeeShiftAssignmentService(EmployeeShiftAssignmentRepository assignmentRepository,
                                          EmployeeRepository employeeRepository,
                                          ShiftRepository shiftRepository) {
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
    }

    // ---------- ASSIGN ONE DAY (entity, used internally) ----------

    @Transactional
    public EmployeeShiftAssignment assignShift(Long employeeId,
                                               Long shiftId,
                                               LocalDate from,
                                               LocalDate to,
                                               Boolean exceptionOverride) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found"));

        EmployeeShiftAssignment assignment = new EmployeeShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShift(shift);
        assignment.setEffectiveFrom(from);
        assignment.setEffectiveTo(to);
        assignment.setExceptionOverride(exceptionOverride);

        return assignmentRepository.save(assignment);
    }

    // ---------- ASSIGN ONE DAY (DTO, used by controller) ----------

    @Transactional
    public EmployeeShiftAssignmentDto assignShiftDto(Long employeeId,
                                                     Long shiftId,
                                                     LocalDate from,
                                                     LocalDate to,
                                                     Boolean exceptionOverride) {
        EmployeeShiftAssignment entity =
                assignShift(employeeId, shiftId, from, to, exceptionOverride);
        return toDto(entity);
    }

    // ---------- GET ASSIGNMENTS FOR EMPLOYEE AS DTO ----------

    @Transactional(readOnly = true)
    public List<EmployeeShiftAssignmentDto> getAssignmentsForEmployee(Long employeeId) {
        List<EmployeeShiftAssignment> list =
                assignmentRepository.findByEmployeeId(employeeId);

        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ---------- MAPPERS ----------

    private EmployeeShiftAssignmentDto toDto(EmployeeShiftAssignment a) {
        EmployeeShiftAssignmentDto dto = new EmployeeShiftAssignmentDto();
        dto.setId(a.getId());

        Employee e = a.getEmployee();
        dto.setEmployeeId(e != null ? e.getId() : null);
        dto.setEmployeeCode(resolveEmployeeCode(e));
        dto.setEmployeeName(buildEmployeeName(e));

        dto.setEffectiveFrom(a.getEffectiveFrom());
        dto.setEffectiveTo(a.getEffectiveTo());
        dto.setExceptionOverride(a.getExceptionOverride());

        ShiftSummaryDto shiftDto = null;
        Shift s = a.getShift();
        if (s != null) {
            shiftDto = new ShiftSummaryDto();
            shiftDto.setId(s.getId());
            shiftDto.setCode(s.getCode());
            shiftDto.setName(s.getName());
            shiftDto.setStartTime(s.getStartTime());
            shiftDto.setEndTime(s.getEndTime());
            shiftDto.setNightShift(s.getNightShift());
            shiftDto.setGeoLatitude(s.getGeoLatitude());
            shiftDto.setGeoLongitude(s.getGeoLongitude());
            shiftDto.setGeoRadiusMeters(s.getGeoRadiusMeters());
        }
        dto.setShift(shiftDto);

        return dto;
    }

    private String buildEmployeeName(Employee e) {
        if (e == null) {
            return null;
        }
        try {
            var fullNameMethod = e.getClass().getMethod("getFullName");
            Object val = fullNameMethod.invoke(e);
            if (val instanceof String && StringUtils.hasText((String) val)) {
                return (String) val;
            }
        } catch (Exception ignored) {}

        String first = safeCallGetter(e, "getFirstName");
        String last = safeCallGetter(e, "getLastName");
        String combined = (first + " " + last).trim();
        return StringUtils.hasText(combined) ? combined : "Employee-" + e.getId();
    }

    private String resolveEmployeeCode(Employee e) {
        if (e == null) {
            return null;
        }
        String code = safeCallGetter(e, "getEmployeeCode");
        if (!StringUtils.hasText(code)) {
            code = safeCallGetter(e, "getCode");
        }
        return StringUtils.hasText(code) ? code : "EMP-" + e.getId();
    }

    private String safeCallGetter(Employee e, String methodName) {
        try {
            var m = e.getClass().getMethod(methodName);
            Object val = m.invoke(e);
            if (val instanceof String && StringUtils.hasText((String) val)) {
                return (String) val;
            }
        } catch (Exception ignored) {}
        return "";
    }
}
