package com.uptrix.uptrix_backend.service.payroll;

import com.uptrix.uptrix_backend.dto.payroll.BonusDto;
import com.uptrix.uptrix_backend.dto.payroll.ClaimDto;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.payroll.Bonus;
import com.uptrix.uptrix_backend.entity.payroll.ReimbursementClaim;
import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.entity.payroll.PayrollRun;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.payroll.BonusRepository;
import com.uptrix.uptrix_backend.repository.payroll.ReimbursementClaimRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollEarningRepository;
import com.uptrix.uptrix_backend.repository.payroll.PayrollRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to handle employee-submitted payroll extras:
 *  - Reimbursement claims
 *  - Bonuses
 *
 * Also contains helper to attach approved extras to a payroll run (create PayrollEarning rows).
 */
@Service
public class PayrollExtrasService {

    private final ReimbursementClaimRepository claimRepository;
    private final BonusRepository bonusRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEarningRepository payrollEarningRepository;

    public PayrollExtrasService(ReimbursementClaimRepository claimRepository,
                                BonusRepository bonusRepository,
                                EmployeeRepository employeeRepository,
                                PayrollRunRepository payrollRunRepository,
                                PayrollEarningRepository payrollEarningRepository) {
        this.claimRepository = claimRepository;
        this.bonusRepository = bonusRepository;
        this.employeeRepository = employeeRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEarningRepository = payrollEarningRepository;
    }

    @Transactional
    public ClaimDto submitClaim(ClaimDto dto) {
        if (dto == null) throw new IllegalArgumentException("ClaimDto is required");

        ReimbursementClaim c = new ReimbursementClaim();

        Employee emp = employeeRepository.findById(dto.getEmployeeId()).orElse(null);
        Company comp = emp != null ? emp.getCompany() : null;

        c.setCompany(comp);
        c.setEmployee(emp);
        c.setAmount(dto.getAmount());
        c.setDescription(dto.getDescription());
        c.setStatus("SUBMITTED");
        c.setSubmittedAt(LocalDateTime.now());
        ReimbursementClaim saved = claimRepository.save(c);

        dto.setId(saved.getId());
        dto.setStatus(saved.getStatus());
        dto.setCompanyId(comp != null ? comp.getId() : null);

        return dto;
    }

    @Transactional
    public BonusDto submitBonus(BonusDto dto) {
        if (dto == null) throw new IllegalArgumentException("BonusDto is required");

        Bonus b = new Bonus();
        Employee emp = employeeRepository.findById(dto.getEmployeeId()).orElse(null);
        Company comp = emp != null ? emp.getCompany() : null;

        b.setCompany(comp);
        b.setEmployee(emp);
        b.setAmount(dto.getAmount());
        b.setReason(dto.getReason());
        b.setStatus("PENDING");
        b.setCreatedAt(LocalDateTime.now());
        Bonus saved = bonusRepository.save(b);

        dto.setId(saved.getId());
        dto.setStatus(saved.getStatus());
        dto.setCompanyId(comp != null ? comp.getId() : null);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ClaimDto> listClaims(Long companyId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        List<ReimbursementClaim> list = claimRepository.findByCompanyIdAndStatusInOrderBySubmittedAtDesc(
                companyId, List.of("SUBMITTED", "APPROVED")
        );

        return list.stream().map(c -> {
            ClaimDto d = new ClaimDto();
            d.setId(c.getId());
            d.setCompanyId(c.getCompany() != null ? c.getCompany().getId() : null);
            d.setEmployeeId(c.getEmployee() != null ? c.getEmployee().getId() : null);
            d.setAmount(c.getAmount());
            d.setDescription(c.getDescription());
            d.setStatus(c.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BonusDto> listBonuses(Long companyId) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        List<Bonus> list = bonusRepository.findByCompanyIdAndStatusInOrderByCreatedAtDesc(
                companyId, List.of("PENDING", "APPROVED")
        );

        return list.stream().map(b -> {
            BonusDto d = new BonusDto();
            d.setId(b.getId());
            d.setCompanyId(b.getCompany() != null ? b.getCompany().getId() : null);
            d.setEmployeeId(b.getEmployee() != null ? b.getEmployee().getId() : null);
            d.setAmount(b.getAmount());
            d.setReason(b.getReason());
            d.setStatus(b.getStatus());
            return d;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void approveClaim(Long claimId, Long approverId) {
        ReimbursementClaim c = claimRepository.findById(claimId).orElseThrow(() ->
                new IllegalArgumentException("Claim not found: " + claimId));
        c.setStatus("APPROVED");
        c.setApprovedBy(approverId);
        c.setProcessedAt(LocalDateTime.now());
        claimRepository.save(c);
    }

    @Transactional
    public void approveBonus(Long bonusId, Long approverId) {
        Bonus b = bonusRepository.findById(bonusId).orElseThrow(() ->
                new IllegalArgumentException("Bonus not found: " + bonusId));
        b.setStatus("APPROVED");
        b.setApprovedBy(approverId);
        b.setProcessedAt(LocalDateTime.now());
        bonusRepository.save(b);
    }

    /**
     * Attach approved bonuses/claims for the given employee to the payroll run.
     *
     * This method:
     *  - Finds the PayrollRun by id,
     *  - Finds APPROVED bonuses/claims for the employee,
     *  - Creates PayrollEarning rows (componentCode/componentName) and saves them,
     *  - Marks the bonus/claim as PAID and sets processedAt.
     *
     * IMPORTANT: call this after the PayrollRun has been created and before finalizing totals.
     */
    @Transactional
    public void attachApprovedExtrasToRun(Long payrollRunId, Long employeeId, int year, int month) {
        if (payrollRunId == null) throw new IllegalArgumentException("payrollRunId required");
        if (employeeId == null) throw new IllegalArgumentException("employeeId required");

        PayrollRun run = payrollRunRepository.findById(payrollRunId).orElse(null);
        if (run == null) return;

        // ----- BONUSES -----
        List<Bonus> bonuses = bonusRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .filter(b -> "APPROVED".equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());

        for (Bonus b : bonuses) {
            PayrollEarning e = new PayrollEarning();
            e.setPayrollRun(run);                        // link to run
            e.setCompany(run.getCompany());              // set company
            e.setEmployee(b.getEmployee());              // set employee
            e.setYear(year);
            e.setMonth(month);
            e.setComponentCode("BONUS");                 // component_code
            e.setComponentName("Approved Bonus");        // component_name
            e.setAmount(b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO);
            e.setCurrency("INR");
            e.setSource("BONUS_APPROVAL");
            payrollEarningRepository.save(e);

            // mark bonus as PAID
            b.setStatus("PAID");
            b.setProcessedAt(LocalDateTime.now());
            bonusRepository.save(b);
        }

        // ----- REIMBURSEMENT CLAIMS -----
        List<ReimbursementClaim> claims = claimRepository.findByEmployeeIdOrderBySubmittedAtDesc(employeeId)
                .stream()
                .filter(cl -> "APPROVED".equalsIgnoreCase(cl.getStatus()))
                .collect(Collectors.toList());

        for (ReimbursementClaim cl : claims) {
            PayrollEarning e = new PayrollEarning();
            e.setPayrollRun(run);
            e.setCompany(run.getCompany());
            e.setEmployee(cl.getEmployee());
            e.setYear(year);
            e.setMonth(month);
            e.setComponentCode("REIMBURSEMENT");
            e.setComponentName("Reimbursement: " + (cl.getDescription() != null ? cl.getDescription() : ""));
            e.setAmount(cl.getAmount() != null ? cl.getAmount() : BigDecimal.ZERO);
            e.setCurrency("INR");
            e.setSource("REIMBURSEMENT_APPROVAL");
            payrollEarningRepository.save(e);

            // mark claim as PAID
            cl.setStatus("PAID");
            cl.setProcessedAt(LocalDateTime.now());
            claimRepository.save(cl);
        }
    }
}
