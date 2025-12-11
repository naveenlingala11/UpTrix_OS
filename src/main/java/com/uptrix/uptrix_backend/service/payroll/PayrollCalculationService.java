package com.uptrix.uptrix_backend.service.payroll;

import com.uptrix.uptrix_backend.dto.attendance.AttendanceDayDto;
import com.uptrix.uptrix_backend.dto.payroll.*;
import com.uptrix.uptrix_backend.dto.shift.EmployeeShiftAssignmentDto;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.payroll.PayrollDeduction;
import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.entity.payroll.PayrollRun;
import com.uptrix.uptrix_backend.entity.payroll.SalaryStructureComponent;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollDeductionRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollEarningRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollRunRepository;
import com.uptrix.uptrix_backend.repository.payroll.SalaryStructureRepository;
import com.uptrix.uptrix_backend.repository.EmployeeShiftAssignmentRepository;
import com.uptrix.uptrix_backend.service.attendance.AttendanceQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollCalculationService {

    private final EmployeeRepository employeeRepository;
    private final SalaryStructureService salaryStructureService;
    private final SalaryStructureRepository salaryStructureRepository;
    private final NightAllowanceService nightAllowanceService;
    private final AttendanceQueryService attendanceQueryService;
    private final EmployeeShiftAssignmentRepository employeeShiftAssignmentRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEarningRepository payrollEarningRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;
    private final TaxSlabService taxSlabService;
    private final PayrollExtrasService payrollExtrasService;

    public PayrollCalculationService(EmployeeRepository employeeRepository,
                                     SalaryStructureService salaryStructureService,
                                     SalaryStructureRepository salaryStructureRepository,
                                     NightAllowanceService nightAllowanceService,
                                     AttendanceQueryService attendanceQueryService,
                                     EmployeeShiftAssignmentRepository employeeShiftAssignmentRepository,
                                     PayrollRunRepository payrollRunRepository,
                                     PayrollEarningRepository payrollEarningRepository,
                                     PayrollDeductionRepository payrollDeductionRepository,
                                     TaxSlabService taxSlabService, PayrollExtrasService payrollExtrasService) {
        this.employeeRepository = employeeRepository;
        this.salaryStructureService = salaryStructureService;
        this.salaryStructureRepository = salaryStructureRepository;
        this.nightAllowanceService = nightAllowanceService;
        this.attendanceQueryService = attendanceQueryService;
        this.employeeShiftAssignmentRepository = employeeShiftAssignmentRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEarningRepository = payrollEarningRepository;
        this.payrollDeductionRepository = payrollDeductionRepository;
        this.taxSlabService = taxSlabService;
        this.payrollExtrasService = payrollExtrasService;
    }

    // ---------- PUBLIC API ----------

    @Transactional(readOnly = true)
    public PayrollPreviewDto previewEmployeeMonth(Long employeeId, int year, int month) {
        if (employeeId == null) {
            throw new IllegalArgumentException("employeeId is required");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDate periodStart = LocalDate.of(year, month, 1);

        // 1) Salary structure
        SalaryStructureDto structureDto =
                salaryStructureService.getCurrentForEmployee(employeeId, periodStart);
        if (structureDto == null) {
            throw new IllegalStateException("No active salary structure for employee");
        }

        // Basic figures
        double basicAmount = resolveBasic(structureDto);
        String currency = structureDto.getCurrency() != null ? structureDto.getCurrency() : "INR";

        // 2) EARNINGS from structure
        List<PayrollEarningDto> earnings = new ArrayList<>();
        double grossEarnings = 0.0;
        Long companyId = employee.getCompany() != null ? employee.getCompany().getId() : null;
        boolean useTaxSlabs = companyId != null && taxSlabService.hasActiveSlabs(companyId);
        double monthlyTaxableEarnings = 0.0;

        Map<String, SalaryStructureComponentDto> componentByCode =
                indexComponentsByCode(structureDto);

        for (SalaryStructureComponentDto c : structureDto.getComponents()) {
            if (!"EARNING".equalsIgnoreCase(c.getComponentType())) {
                continue;
            }
            double amount = calculateComponentAmount(c, basicAmount);
            if (amount <= 0.0) continue;

            PayrollEarningDto e = new PayrollEarningDto();
            e.setEmployeeId(employeeId);
            e.setEmployeeName(buildEmployeeName(employee));
            e.setEmployeeCode(resolveEmployeeCode(employee));

            e.setYear(year);
            e.setMonth(month);
            e.setComponentCode(c.getComponentCode());
            e.setComponentName(c.getComponentName());
            e.setAmount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
            e.setCurrency(currency);
            e.setStatus("PREVIEW");
            e.setLocked(false);
            e.setSource("STRUCTURE");

            earnings.add(e);
            grossEarnings += amount;

            // track taxable portion
            if (Boolean.TRUE.equals(c.getTaxable())) {
                monthlyTaxableEarnings += amount;
            }
        }

        // 3) NIGHT ALLOWANCE earning (preview only, unless generated via NightAllowanceService)
        double nightAmount = calculateNightAllowance(employeeId, year, month);
        if (nightAmount > 0.0) {
            PayrollEarningDto nightE = new PayrollEarningDto();
            nightE.setEmployeeId(employeeId);
            nightE.setEmployeeName(buildEmployeeName(employee));
            nightE.setEmployeeCode(resolveEmployeeCode(employee));
            nightE.setYear(year);
            nightE.setMonth(month);
            nightE.setComponentCode("NIGHT_ALLOWANCE");
            nightE.setComponentName("Night Shift Allowance");
            nightE.setAmount(BigDecimal.valueOf(nightAmount).setScale(2, RoundingMode.HALF_UP));
            nightE.setCurrency(currency);
            nightE.setStatus("PREVIEW");
            nightE.setLocked(false);
            nightE.setSource("NIGHT_ALLOWANCE_PREVIEW");

            earnings.add(nightE);
            grossEarnings += nightAmount;
            monthlyTaxableEarnings += nightAmount;
        }

        // 4) OVERTIME earning
        double otAmount = calculateOvertimeAmount(employeeId, year, month, structureDto);
        if (otAmount > 0.0) {
            PayrollEarningDto otE = new PayrollEarningDto();
            otE.setEmployeeId(employeeId);
            otE.setEmployeeName(buildEmployeeName(employee));
            otE.setEmployeeCode(resolveEmployeeCode(employee));
            otE.setYear(year);
            otE.setMonth(month);
            otE.setComponentCode("OT");
            otE.setComponentName("Overtime");
            otE.setAmount(BigDecimal.valueOf(otAmount).setScale(2, RoundingMode.HALF_UP));
            otE.setCurrency(currency);
            otE.setStatus("PREVIEW");
            otE.setLocked(false);
            otE.setSource("OVERTIME_CALC");

            earnings.add(otE);
            grossEarnings += otAmount;
            monthlyTaxableEarnings += otAmount;
        }

        // 5) DEDUCTIONS from structure (PF / ESI / TDS etc.)
        List<PayrollDeductionDto> deductions = new ArrayList<>();
        double totalDeductions = 0.0;

        // flag to see if structure had a TDS component (for fallback)
        Double structureTdsMonthlyAmount = null;

        for (SalaryStructureComponentDto c : structureDto.getComponents()) {
            if (!"DEDUCTION".equalsIgnoreCase(c.getComponentType())) {
                continue;
            }

            boolean isTdsComponent = "TDS".equalsIgnoreCase(c.getComponentCode());

            double amount = calculateComponentAmount(c, basicAmount);
            if (amount <= 0.0) continue;

            if (isTdsComponent) {
                // just remember the amount for potential fallback
                structureTdsMonthlyAmount = amount;
                // if we are using slabs, skip adding TDS from structure
                if (useTaxSlabs) {
                    continue;
                }
            }

            PayrollDeductionDto d = new PayrollDeductionDto();
            d.setCode(c.getComponentCode());
            d.setName(c.getComponentName());
            d.setAmount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP));
            d.setCurrency(currency);

            deductions.add(d);
            totalDeductions += amount;
        }

        // --- TDS from Tax Slabs (if configured) ---
        if (useTaxSlabs && companyId != null) {
            BigDecimal taxableAnnual = BigDecimal
                    .valueOf(monthlyTaxableEarnings)
                    .multiply(BigDecimal.valueOf(12))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal monthlyTds = taxSlabService.calculateMonthlyTds(companyId, taxableAnnual);

            if (monthlyTds.compareTo(BigDecimal.ZERO) > 0) {
                PayrollDeductionDto tdsDto = new PayrollDeductionDto();
                tdsDto.setCode("TDS");
                tdsDto.setName("TDS (Tax Slabs)");
                tdsDto.setAmount(monthlyTds);
                tdsDto.setCurrency(currency);

                deductions.add(tdsDto);
                totalDeductions += monthlyTds.doubleValue();
            }
        } else if (!useTaxSlabs && structureTdsMonthlyAmount != null && structureTdsMonthlyAmount > 0.0) {
            // no slabs configured: fallback to structure TDS
            PayrollDeductionDto tdsDto = new PayrollDeductionDto();
            tdsDto.setCode("TDS");
            tdsDto.setName("TDS");
            tdsDto.setAmount(BigDecimal.valueOf(structureTdsMonthlyAmount).setScale(2, RoundingMode.HALF_UP));
            tdsDto.setCurrency(currency);

            deductions.add(tdsDto);
            totalDeductions += structureTdsMonthlyAmount;
        }

        BigDecimal gross = BigDecimal.valueOf(grossEarnings).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ded = BigDecimal.valueOf(totalDeductions).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(ded);

        PayrollPreviewDto preview = new PayrollPreviewDto();
        preview.setEmployeeId(employeeId);
        preview.setEmployeeCode(resolveEmployeeCode(employee));
        preview.setEmployeeName(buildEmployeeName(employee));
        preview.setYear(year);
        preview.setMonth(month);
        preview.setGrossEarnings(gross);
        preview.setTotalDeductions(ded);
        preview.setNetPay(net);
        preview.setEarnings(earnings);
        preview.setDeductions(deductions);

        return preview;

    }

    @Transactional
    public PayrollPreviewDto generatePayrollForEmployee(Long employeeId, int year, int month, String runType) {
        PayrollPreviewDto preview = previewEmployeeMonth(employeeId, year, month);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (employee.getCompany() == null) {
            throw new IllegalStateException("Employee has no company linked");
        }

        // 1) Get or create PayrollRun
        PayrollRun run = payrollRunRepository
                .findByCompanyIdAndYearAndMonth(employee.getCompany().getId(), year, month)
                .orElseGet(() -> {
                    PayrollRun r = new PayrollRun();
                    r.setCompany(employee.getCompany());
                    r.setYear(year);
                    r.setMonth(month);
                    r.setRunType(runType != null ? runType : "REGULAR");
                    r.setStatus("DRAFT");
                    r.setCreatedBy(employee); // or current user
                    r.setCreatedAt(java.time.LocalDateTime.now());
                    return payrollRunRepository.save(r);
                });

        // FIXED: use 'run' variable (not payrollRun)
        payrollExtrasService.attachApprovedExtrasToRun(run.getId(), employeeId, year, month);

        // 2) Persist earnings (from preview) as PayrollEarning
        for (PayrollEarningDto eDto : preview.getEarnings()) {
            PayrollEarning earning = new PayrollEarning();
            earning.setEmployee(employee);
            earning.setCompany(employee.getCompany());
            earning.setYear(year);
            earning.setMonth(month);
            earning.setComponentCode(eDto.getComponentCode());
            earning.setComponentName(eDto.getComponentName());
            earning.setAmount(eDto.getAmount());
            earning.setCurrency(eDto.getCurrency());
            earning.setStatus("CALCULATED");
            earning.setLocked(false);
            earning.setSource(eDto.getSource() != null ? eDto.getSource() : "PAYROLL_CALC");
            earning.setPayrollRun(run);
            payrollEarningRepository.save(earning);
        }

        // 3) Persist deductions as PayrollDeduction
        for (PayrollDeductionDto dDto : preview.getDeductions()) {
            PayrollDeduction d = new PayrollDeduction();
            d.setPayrollRun(run);
            d.setEmployee(employee);
            d.setCode(dDto.getCode());
            d.setName(dDto.getName());
            d.setAmount(dDto.getAmount());
            d.setCurrency(dDto.getCurrency());
            payrollDeductionRepository.save(d);
        }

        return preview;
    }

    // ---------- INTERNAL HELPERS ----------

    private double resolveBasic(SalaryStructureDto structure) {
        if (structure == null || structure.getComponents() == null) {
            return 0.0;
        }
        return structure.getComponents().stream()
                .filter(c -> "EARNING".equalsIgnoreCase(c.getComponentType()))
                .filter(c -> "BASIC".equalsIgnoreCase(c.getComponentCode()))
                .map(c -> c.getAmountValue() != null ? c.getAmountValue() : 0.0)
                .findFirst()
                .orElse(0.0);
    }

    private Map<String, SalaryStructureComponentDto> indexComponentsByCode(SalaryStructureDto structure) {
        Map<String, SalaryStructureComponentDto> map = new HashMap<>();
        if (structure != null && structure.getComponents() != null) {
            for (SalaryStructureComponentDto c : structure.getComponents()) {
                if (c.getComponentCode() != null) {
                    map.put(c.getComponentCode().toUpperCase(Locale.ROOT), c);
                }
            }
        }
        return map;
    }

    private double calculateComponentAmount(SalaryStructureComponentDto c, double basicAmount) {
        if (c == null) return 0.0;
        String calcType = c.getCalculationType() != null ? c.getCalculationType().toUpperCase(Locale.ROOT) : "FIXED";

        if ("FIXED".equals(calcType)) {
            return c.getAmountValue() != null ? c.getAmountValue() : 0.0;
        } else if ("PERCENT_OF_BASIC".equals(calcType)) {
            if (c.getPercentOfBasic() == null) return 0.0;
            return basicAmount * c.getPercentOfBasic() / 100.0;
        } else {
            // TODO: support FORMULA in future
            return 0.0;
        }
    }

    private double calculateNightAllowance(Long employeeId, int year, int month) {
        try {
            NightAllowancePreviewRequest req = new NightAllowancePreviewRequest();
            req.setEmployeeId(employeeId);
            req.setYear(year);
            req.setMonth(month);

            NightAllowancePreviewResponse resp = nightAllowanceService.preview(req);
            if (resp == null || resp.getTotalAmount() == null) {
                return 0.0;
            }
            return resp.getTotalAmount().doubleValue();
        } catch (Exception ex) {
            // if anything fails, just skip night allowance in preview
            return 0.0;
        }
    }

    private double calculateOvertimeAmount(Long employeeId, int year, int month, SalaryStructureDto structureDto) {
        // Find OT rate from structure: componentCode "OT" with FIXED amountValue as hourly rate
        double otRatePerHour = 0.0;
        if (structureDto != null && structureDto.getComponents() != null) {
            for (SalaryStructureComponentDto c : structureDto.getComponents()) {
                if ("EARNING".equalsIgnoreCase(c.getComponentType())
                        && "OT".equalsIgnoreCase(c.getComponentCode())
                        && c.getAmountValue() != null
                ) {
                    otRatePerHour = c.getAmountValue();
                    break;
                }
            }
        }
        if (otRatePerHour <= 0.0) {
            return 0.0;
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // Attendance per day
        List<AttendanceDayDto> attendanceList =
                attendanceQueryService.getMonthForEmployee(employeeId, year, month);

        // Shift assignments in range
        List<com.uptrix.uptrix_backend.entity.EmployeeShiftAssignment> assignments =
                employeeShiftAssignmentRepository.findAssignmentsForEmployeeInRange(
                        employeeId,
                        start,
                        end
                );

        // Index shift expected minutes per date
        Map<LocalDate, Integer> expectedMinutesByDate = new HashMap<>();
        for (com.uptrix.uptrix_backend.entity.EmployeeShiftAssignment a : assignments) {
            if (a.getShift() == null || a.getEffectiveFrom() == null) continue;
            LocalDate from = a.getEffectiveFrom();
            LocalDate to = a.getEffectiveTo() != null ? a.getEffectiveTo() : from;

            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                if (d.isBefore(start) || d.isAfter(end)) continue;
                int expected = getExpectedMinutesForShift(a.getShift());
                expectedMinutesByDate.put(d, expected);
            }
        }

        long totalOtMinutes = 0L;
        int threshold = 15; // min overtime threshold

        for (AttendanceDayDto day : attendanceList) {
            if (day.getDate() == null || day.getWorkedMinutes() == null) continue;
            Integer expected = expectedMinutesByDate.get(day.getDate());
            if (expected == null || expected <= 0) continue;

            long diff = day.getWorkedMinutes() - expected;
            if (diff > threshold) {
                totalOtMinutes += diff;
            }
        }

        if (totalOtMinutes <= 0) {
            return 0.0;
        }

        double otHours = totalOtMinutes / 60.0;
        return otHours * otRatePerHour;
    }

    private Integer toMinutes(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.getHour() * 60 + time.getMinute();
    }

    @Transactional
    public PayrollBatchResultDto generatePayrollForCompany(Long companyId,
                                                           int year,
                                                           int month,
                                                           String runType,
                                                           java.util.List<Long> employeeIds) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required for batch payroll");
        }

        // Get employees: if employeeIds provided -> filter, else all employees of company
        java.util.List<Employee> allEmployees = employeeRepository.findAll();
        java.util.List<Employee> targetEmployees = new java.util.ArrayList<>();

        for (Employee e : allEmployees) {
            if (e.getCompany() == null || e.getCompany().getId() == null) continue;
            if (!e.getCompany().getId().equals(companyId)) continue;

            if (employeeIds != null && !employeeIds.isEmpty()) {
                if (!employeeIds.contains(e.getId())) {
                    continue;
                }
            }
            targetEmployees.add(e);
        }

        java.util.List<EmployeePayrollResultDto> results = new java.util.ArrayList<>();

        for (Employee e : targetEmployees) {
            EmployeePayrollResultDto r = new EmployeePayrollResultDto();
            r.setEmployeeId(e.getId());
            r.setEmployeeCode(resolveEmployeeCode(e));
            r.setEmployeeName(buildEmployeeName(e));

            try {
                generatePayrollForEmployee(e.getId(), year, month, runType);
                r.setSuccess(true);
                r.setErrorMessage(null);
            } catch (Exception ex) {
                r.setSuccess(false);
                r.setErrorMessage(ex.getMessage());
            }

            results.add(r);
        }

        // Find the run we just used/created (all employees share same run for that company/year/month)
        PayrollRun run = payrollRunRepository
                .findByCompanyIdAndYearAndMonth(companyId, year, month)
                .orElse(null);

        PayrollBatchResultDto batch = new PayrollBatchResultDto();
        batch.setCompanyId(companyId);
        batch.setYear(year);
        batch.setMonth(month);
        batch.setRunType(runType);
        batch.setPayrollRunId(run != null ? run.getId() : null);
        batch.setProcessedCount(results.size());
        batch.setResults(results);

        return batch;
    }

    private int getExpectedMinutesForShift(com.uptrix.uptrix_backend.entity.Shift shift) {
        if (shift == null || shift.getStartTime() == null || shift.getEndTime() == null) {
            return 0;
        }
        Integer startMinutes = toMinutes(shift.getStartTime()); // LocalTime overload
        Integer endMinutes = toMinutes(shift.getEndTime());
        if (startMinutes == null || endMinutes == null) return 0;

        int e = endMinutes;
        if (e <= startMinutes) {
            e += 24 * 60; // overnight
        }
        return e - startMinutes;
    }

    private Integer toMinutes(String hhmm) {
        if (!StringUtils.hasText(hhmm)) return null;
        String[] parts = hhmm.split(":");
        if (parts.length < 2) return null;
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return h * 60 + m;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildEmployeeName(Employee e) {
        if (e == null) return null;
        try {
            var m = e.getClass().getMethod("getFullName");
            Object val = m.invoke(e);
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
        if (e == null) return null;
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
