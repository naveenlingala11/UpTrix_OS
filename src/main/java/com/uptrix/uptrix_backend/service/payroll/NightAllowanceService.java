package com.uptrix.uptrix_backend.service.payroll;

import com.uptrix.uptrix_backend.dto.payroll.NightAllowanceGenerateRequest;
import com.uptrix.uptrix_backend.dto.payroll.NightAllowancePreviewRequest;
import com.uptrix.uptrix_backend.dto.payroll.NightAllowancePreviewResponse;
import com.uptrix.uptrix_backend.dto.payroll.PayrollEarningDto;
import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.EmployeeShiftAssignment;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.repository.AttendanceRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.EmployeeShiftAssignmentRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollEarningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NightAllowanceService {

    private static final String COMPONENT_CODE = "NIGHT_ALLOWANCE";
    private static final String COMPONENT_NAME = "Night Shift Allowance";

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeShiftAssignmentRepository assignmentRepository;
    private final PayrollEarningRepository payrollEarningRepository;

    public NightAllowanceService(EmployeeRepository employeeRepository,
                                 AttendanceRepository attendanceRepository,
                                 EmployeeShiftAssignmentRepository assignmentRepository,
                                 PayrollEarningRepository payrollEarningRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.payrollEarningRepository = payrollEarningRepository;
    }

    // ---------- PREVIEW CALCULATION (unchanged, just moved) ----------

    @Transactional(readOnly = true)
    public NightAllowancePreviewResponse preview(NightAllowancePreviewRequest req) {
        if (req == null || req.getEmployeeId() == null) {
            throw new IllegalArgumentException("employeeId is required");
        }
        if (req.getYear() == null || req.getMonth() == null) {
            throw new IllegalArgumentException("year and month are required");
        }
        int year = req.getYear();
        int month = req.getMonth();
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }

        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Attendance> attendanceList =
                attendanceRepository.findByEmployeeAndDateBetween(employee, start, end);
        Map<LocalDate, Attendance> attendanceMap = attendanceList.stream()
                .collect(Collectors.toMap(Attendance::getDate, a -> a));

        List<EmployeeShiftAssignment> assignments =
                assignmentRepository.findAssignmentsForEmployeeInRange(employee.getId(), start, end);

        Map<LocalDate, Shift> shiftPerDate = new HashMap<>();
        for (EmployeeShiftAssignment a : assignments) {
            LocalDate from = a.getEffectiveFrom();
            LocalDate to = (a.getEffectiveTo() != null) ? a.getEffectiveTo() : a.getEffectiveFrom();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                if ((d.isBefore(start) || d.isAfter(end))) {
                    continue;
                }
                Shift existing = shiftPerDate.get(d);
                if (existing == null || Boolean.TRUE.equals(a.getExceptionOverride())) {
                    shiftPerDate.put(d, a.getShift());
                }
            }
        }

        NightAllowancePreviewResponse response = new NightAllowancePreviewResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeName(buildEmployeeName(employee));
        response.setEmployeeCode(resolveEmployeeCode(employee));
        response.setYear(year);
        response.setMonth(month);
        response.setTotalDaysInMonth(start.lengthOfMonth());

        List<NightAllowancePreviewResponse.DayBreakdown> days = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int eligibleDays = 0;

        List<BigDecimal> perDayAmounts = new ArrayList<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            NightAllowancePreviewResponse.DayBreakdown day = new NightAllowancePreviewResponse.DayBreakdown();
            day.setDate(d);

            Attendance att = attendanceMap.get(d);
            Shift shift = shiftPerDate.get(d);

            boolean hasShift = shift != null;
            day.setHasShift(hasShift);
            if (shift != null) {
                day.setShiftCode(shift.getCode());
                day.setShiftNightFlag(shift.getNightShift());
                day.setShiftAutoAllowance(shift.getAutoNightAllowance());
                day.setShiftAllowanceAmount(shift.getNightAllowanceAmount());
            }

            boolean hasAttendance = att != null;
            day.setHasAttendance(hasAttendance);
            String attendanceStatus = (att != null && StringUtils.hasText(att.getStatus()))
                    ? att.getStatus()
                    : (att != null ? "PRESENT" : "ABSENT");
            day.setAttendanceStatus(attendanceStatus);

            boolean eligible = false;
            BigDecimal earned = BigDecimal.ZERO;

            if (shift != null &&
                    Boolean.TRUE.equals(shift.getAutoNightAllowance()) &&
                    shift.getNightAllowanceAmount() != null &&
                    att != null &&
                    isPresentLike(attendanceStatus) &&
                    att.getCheckInTime() != null) {

                eligible = true;
                earned = shift.getNightAllowanceAmount();
            }

            day.setEligible(eligible);
            day.setEarnedAmount(earned);

            if (eligible) {
                eligibleDays++;
                totalAmount = totalAmount.add(earned);
                perDayAmounts.add(earned);
            }

            days.add(day);
        }

        response.setEligibleDays(eligibleDays);
        response.setTotalAmount(totalAmount);

        if (!perDayAmounts.isEmpty()
                && perDayAmounts.stream().distinct().count() == 1) {
            response.setConstantPerDayAmount(perDayAmounts.get(0));
        }

        response.setDays(days);
        return response;
    }

    // ---------- PERSISTING AS PAYROLL EARNING ----------

    @Transactional
    public PayrollEarning generateEarning(NightAllowanceGenerateRequest req) {
        if (req == null || req.getEmployeeId() == null) {
            throw new IllegalArgumentException("employeeId is required");
        }
        if (req.getYear() == null || req.getMonth() == null) {
            throw new IllegalArgumentException("year and month are required");
        }

        NightAllowancePreviewRequest previewRequest = new NightAllowancePreviewRequest();
        previewRequest.setEmployeeId(req.getEmployeeId());
        previewRequest.setYear(req.getYear());
        previewRequest.setMonth(req.getMonth());

        NightAllowancePreviewResponse preview = preview(previewRequest);

        Employee employee = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        boolean overwrite = Boolean.TRUE.equals(req.getOverwriteExisting());

        // If already exists
        Optional<PayrollEarning> existingOpt =
                payrollEarningRepository.findByEmployeeAndYearAndMonthAndComponentCode(
                        employee, preview.getYear(), preview.getMonth(), COMPONENT_CODE
                );

        PayrollEarning earning;
        if (existingOpt.isPresent()) {
            earning = existingOpt.get();
            if (Boolean.TRUE.equals(earning.getLocked()) && !overwrite) {
                throw new IllegalStateException("Earning is locked and cannot be overwritten");
            }
            if (!overwrite) {
                // Just return existing without changing
                return earning;
            }
        } else {
            earning = new PayrollEarning();
            earning.setEmployee(employee);
            earning.setCompany(employee.getCompany());
            earning.setYear(preview.getYear());
            earning.setMonth(preview.getMonth());
            earning.setComponentCode(COMPONENT_CODE);
            earning.setComponentName(COMPONENT_NAME);
            earning.setCurrency("INR"); // or fetch from company/employee
            earning.setSource("NIGHT_ALLOWANCE_AUTO");
        }

        earning.setAmount(preview.getTotalAmount());
        earning.setStatus("GENERATED");
        earning.setLocked(Boolean.FALSE);

        return payrollEarningRepository.save(earning);
    }

    // ---------- Helpers ----------

    private boolean isPresentLike(String status) {
        if (!StringUtils.hasText(status)) {
            return false;
        }
        String s = status.trim().toUpperCase();
        return "PRESENT".equals(s) || "HALF_DAY".equals(s);
    }

    private String buildEmployeeName(Employee e) {
        try {
            java.lang.reflect.Method fullNameMethod = e.getClass().getMethod("getFullName");
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
        String code = safeCallGetter(e, "getEmployeeCode");
        if (!StringUtils.hasText(code)) {
            code = safeCallGetter(e, "getCode");
        }
        return StringUtils.hasText(code) ? code : "EMP-" + e.getId();
    }

    private String safeCallGetter(Employee e, String methodName) {
        try {
            java.lang.reflect.Method m = e.getClass().getMethod(methodName);
            Object val = m.invoke(e);
            if (val instanceof String && StringUtils.hasText((String) val)) {
                return (String) val;
            }
        } catch (Exception ignored) {}
        return "";
    }

    // ---------- Mapper to DTO ----------

    public PayrollEarningDto toDto(PayrollEarning earning) {
        PayrollEarningDto dto = new PayrollEarningDto();
        dto.setId(earning.getId());
        dto.setEmployeeId(earning.getEmployee().getId());
        dto.setEmployeeName(buildEmployeeName(earning.getEmployee()));
        dto.setEmployeeCode(resolveEmployeeCode(earning.getEmployee()));
        dto.setYear(earning.getYear());
        dto.setMonth(earning.getMonth());
        dto.setComponentCode(earning.getComponentCode());
        dto.setComponentName(earning.getComponentName());
        dto.setAmount(earning.getAmount());
        dto.setCurrency(earning.getCurrency());
        dto.setStatus(earning.getStatus());
        dto.setLocked(earning.getLocked());
        dto.setSource(earning.getSource());
        return dto;
    }
}
