package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.payroll.PayrollIncentive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollIncentiveRepository extends JpaRepository<PayrollIncentive, Long> {

    List<PayrollIncentive> findByPayrollRunId(Long runId);
}
