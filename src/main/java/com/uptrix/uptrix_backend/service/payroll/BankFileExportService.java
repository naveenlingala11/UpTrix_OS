package com.uptrix.uptrix_backend.service.payroll;

import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.payroll.PayrollDeduction;
import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.repository.payroll.PayrollDeductionRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollEarningRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class BankFileExportService {

    private final PayrollEarningRepository payrollEarningRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;

    public BankFileExportService(PayrollEarningRepository payrollEarningRepository,
                                 PayrollDeductionRepository payrollDeductionRepository) {
        this.payrollEarningRepository = payrollEarningRepository;
        this.payrollDeductionRepository = payrollDeductionRepository;
    }

    public byte[] generateBankFileCsv(Long payrollRunId) {
        List<PayrollEarning> earnings = payrollEarningRepository.findByPayrollRunId(payrollRunId);
        List<PayrollDeduction> deductions = payrollDeductionRepository.findByPayrollRunId(payrollRunId);

        // Group earnings & deductions by employee
        Map<Long, BigDecimal> earningsByEmp = new HashMap<>();
        Map<Long, BigDecimal> deductionsByEmp = new HashMap<>();
        Map<Long, Employee> employeeById = new HashMap<>();

        for (PayrollEarning e : earnings) {
            if (e.getEmployee() == null) continue;
            Long empId = e.getEmployee().getId();
            employeeById.putIfAbsent(empId, e.getEmployee());
            BigDecimal amount = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            earningsByEmp.merge(empId, amount, BigDecimal::add);
        }

        for (PayrollDeduction d : deductions) {
            if (d.getEmployee() == null) continue;
            Long empId = d.getEmployee().getId();
            employeeById.putIfAbsent(empId, d.getEmployee());
            BigDecimal amount = d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO;
            deductionsByEmp.merge(empId, amount, BigDecimal::add);
        }

        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("EmployeeCode,EmployeeName,BankAccount,NetAmount\n");

        for (Map.Entry<Long, Employee> entry : employeeById.entrySet()) {
            Long empId = entry.getKey();
            Employee emp = entry.getValue();

            BigDecimal totalEarn = earningsByEmp.getOrDefault(empId, BigDecimal.ZERO);
            BigDecimal totalDed = deductionsByEmp.getOrDefault(empId, BigDecimal.ZERO);
            BigDecimal net = totalEarn.subtract(totalDed).setScale(2, RoundingMode.HALF_UP);

            String code = resolveEmployeeCode(emp);
            String name = buildEmployeeName(emp);
            String bankAcc = resolveBankAccount(emp);

            sb.append(escapeCsv(code)).append(",");
            sb.append(escapeCsv(name)).append(",");
            sb.append(escapeCsv(bankAcc)).append(",");
            sb.append(net.toPlainString()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private String buildEmployeeName(Employee e) {
        if (e == null) return "";
        try {
            var m = e.getClass().getMethod("getFullName");
            Object v = m.invoke(e);
            if (v instanceof String && StringUtils.hasText((String) v)) {
                return (String) v;
            }
        } catch (Exception ignored) {}
        String first = safeCallGetter(e, "getFirstName");
        String last = safeCallGetter(e, "getLastName");
        String combined = (first + " " + last).trim();
        return StringUtils.hasText(combined) ? combined : "Employee-" + e.getId();
    }

    private String resolveEmployeeCode(Employee e) {
        if (e == null) return "";
        String code = safeCallGetter(e, "getEmployeeCode");
        if (!StringUtils.hasText(code)) {
            code = safeCallGetter(e, "getCode");
        }
        return StringUtils.hasText(code) ? code : "EMP-" + e.getId();
    }

    // Try a couple of common bank account field names
    private String resolveBankAccount(Employee e) {
        if (e == null) return "";
        String acc = safeCallGetter(e, "getBankAccountNumber");
        if (!StringUtils.hasText(acc)) {
            acc = safeCallGetter(e, "getSalaryAccountNumber");
        }
        return acc != null ? acc : "";
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
