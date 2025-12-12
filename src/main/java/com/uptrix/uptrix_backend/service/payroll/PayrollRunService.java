package com.uptrix.uptrix_backend.service.payroll;

import com.uptrix.uptrix_backend.dto.payroll.MyPayslipDto;
import com.uptrix.uptrix_backend.dto.payroll.PayrollRunDto;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.payroll.PayrollDeduction;
import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.entity.payroll.PayrollRun;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollDeductionRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollRunRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollEarningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEarningRepository payrollEarningRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;

    public PayrollRunService(PayrollRunRepository payrollRunRepository,
                             PayrollEarningRepository payrollEarningRepository,
                             EmployeeRepository employeeRepository,
                             PayrollDeductionRepository payrollDeductionRepository) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEarningRepository = payrollEarningRepository;
        this.employeeRepository = employeeRepository;
        this.payrollDeductionRepository = payrollDeductionRepository;
    }

    @Transactional(readOnly = true)
    public List<PayrollRunDto> listRuns(Long companyId) {
        List<PayrollRun> runs;

        if (companyId != null) {
            runs = payrollRunRepository.findByCompanyIdOrderByYearDescMonthDesc(companyId);
        } else {
            runs = payrollRunRepository.findAll()
                    .stream()
                    .sorted((a, b) -> {
                        int c = b.getYear().compareTo(a.getYear());
                        if (c != 0) return c;
                        return b.getMonth().compareTo(a.getMonth());
                    })
                    .collect(Collectors.toList());
        }

        return runs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollRunDto approveRun(Long runId, Long approverEmployeeId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("PayrollRun not found"));

        Employee approver = null;
        if (approverEmployeeId != null) {
            approver = employeeRepository.findById(approverEmployeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Approver employee not found"));
        }

        run.setStatus("APPROVED");
        run.setApprovedBy(approver);
        run.setApprovedAt(LocalDateTime.now());

        PayrollRun saved = payrollRunRepository.save(run);
        return toDto(saved);
    }

    @Transactional
    public PayrollRunDto lockRun(Long runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("PayrollRun not found"));

        run.setStatus("LOCKED");
        PayrollRun saved = payrollRunRepository.save(run);

        // Lock all earnings in this run
        payrollEarningRepository.findByPayrollRunId(runId)
                .forEach(e -> {
                    e.setLocked(true);
                    payrollEarningRepository.save(e);
                });

        return toDto(saved);
    }

    private PayrollRunDto toDto(PayrollRun run) {
        PayrollRunDto dto = new PayrollRunDto();
        dto.setId(run.getId());
        dto.setCompanyId(run.getCompany() != null ? run.getCompany().getId() : null);
        dto.setYear(run.getYear());
        dto.setMonth(run.getMonth());
        dto.setRunType(run.getRunType());
        dto.setStatus(run.getStatus());
        dto.setCreatedAt(run.getCreatedAt());
        dto.setApprovedAt(run.getApprovedAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<MyPayslipDto> listPayslipsForEmployee(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("employeeId is required");
        }

        List<PayrollRun> runs = payrollEarningRepository.findRunsForEmployee(employeeId);

        List<MyPayslipDto> result = new ArrayList<>();

        for (PayrollRun run : runs) {
            Long runId = run.getId();
            if (runId == null) continue;

            List<PayrollEarning> earnings = payrollEarningRepository
                    .findByPayrollRunIdAndEmployeeId(runId, employeeId);

            List<PayrollDeduction> deductions = payrollDeductionRepository
                    .findByPayrollRunIdAndEmployeeId(runId, employeeId);

            BigDecimal gross = BigDecimal.ZERO;
            for (PayrollEarning e : earnings) {
                if (e.getAmount() != null) {
                    gross = gross.add(e.getAmount());
                }
            }

            BigDecimal totalDed = BigDecimal.ZERO;
            for (PayrollDeduction d : deductions) {
                if (d.getAmount() != null) {
                    totalDed = totalDed.add(d.getAmount());
                }
            }

            BigDecimal net = gross.subtract(totalDed);

            MyPayslipDto dto = new MyPayslipDto();
            dto.setPayrollRunId(runId);
            dto.setYear(run.getYear());
            dto.setMonth(run.getMonth());
            dto.setRunType(run.getRunType());
            dto.setStatus(run.getStatus());
            dto.setGrossEarnings(gross);
            dto.setTotalDeductions(totalDed);
            dto.setNetPay(net);

            result.add(dto);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public byte[] exportRunBankCsv(Long payrollRunId) {
        PayrollRun run = payrollRunRepository.findById(payrollRunId).orElseThrow();
        // load all employees & net pay for run
        List<PayrollEarning> earnings = payrollEarningRepository.findByPayrollRunId(payrollRunId);
        // We need net pay per employee -> compute: gross - deductions
        // Better: ask existing methods that compute net per employee; here compute explicitly.

        Map<Long, BigDecimal> grossByEmp = new HashMap<>();
        Map<Long, BigDecimal> dedByEmp = new HashMap<>();

        // sum earnings
        for (PayrollEarning e : earnings) {
            Long empId = e.getEmployee().getId();
            grossByEmp.put(empId, grossByEmp.getOrDefault(empId, BigDecimal.ZERO).add(e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO));
        }

        List<PayrollDeduction> deductions = payrollDeductionRepository.findByPayrollRunId(payrollRunId);
        for (PayrollDeduction d : deductions) {
            Long empId = d.getEmployee().getId();
            dedByEmp.put(empId, dedByEmp.getOrDefault(empId, BigDecimal.ZERO).add(d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO));
        }

        StringBuilder sb = new StringBuilder();
        // header - banks often require specific order — keep simple
        sb.append("BeneficiaryName,EmployeeCode,EmployeeId,BankAccount,IFSC,NetPay\n");

        // find unique employee ids
        Set<Long> empIds = new HashSet<>();
        empIds.addAll(grossByEmp.keySet());
        empIds.addAll(dedByEmp.keySet());

        for (Long empId : empIds) {
            Employee emp = employeeRepository.findById(empId).orElse(null);
            if (emp == null) continue;
            BigDecimal gross = grossByEmp.getOrDefault(empId, BigDecimal.ZERO);
            BigDecimal ded = dedByEmp.getOrDefault(empId, BigDecimal.ZERO);
            BigDecimal net = gross.subtract(ded).setScale(2, RoundingMode.HALF_UP);

            String acc = emp.getBankAccountNumber() != null ? emp.getBankAccountNumber() : "";
            String ifsc = emp.getIfsc() != null ? emp.getIfsc() : "";
            String code = emp.getEmployeeCode() != null ? emp.getEmployeeCode() : (emp.getCode()!=null?emp.getCode():"EMP-"+empId);
            String name = (emp.getFullName()!=null?emp.getFullName(): ( (emp.getFirstName()!=null?emp.getFirstName():"") + " " + (emp.getLastName()!=null?emp.getLastName():"") ).trim());

            sb.append("\"").append(name.replace("\"","\"\"")).append("\",")
                    .append(code).append(",")
                    .append(empId).append(",")
                    .append(acc).append(",")
                    .append(ifsc).append(",")
                    .append(net.toPlainString())
                    .append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

}
