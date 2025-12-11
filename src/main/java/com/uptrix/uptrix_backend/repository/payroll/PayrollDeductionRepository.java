package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.payroll.PayrollDeduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollDeductionRepository extends JpaRepository<PayrollDeduction, Long> {

    List<PayrollDeduction> findByPayrollRunId(Long runId);

    List<PayrollDeduction> findByPayrollRunIdAndEmployeeId(Long payrollRunId, Long employeeId);

}
