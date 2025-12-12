package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.entity.payroll.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollEarningRepository extends JpaRepository<PayrollEarning, Long> {

    Optional<PayrollEarning> findByEmployeeAndYearAndMonthAndComponentCode(
            Employee employee, Integer year, Integer month, String componentCode
    );

    List<PayrollEarning> findByEmployeeAndYearAndMonth(
            Employee employee, Integer year, Integer month
    );

    List<PayrollEarning> findByPayrollRunId(Long runId);

    List<PayrollEarning> findByPayrollRunIdAndEmployeeId(Long payrollRunId, Long employeeId);

    @Query("select distinct e.payrollRun from PayrollEarning e " +
            "where e.employee.id = :employeeId " +
            "order by e.payrollRun.year desc, e.payrollRun.month desc")
    List<PayrollRun> findRunsForEmployee(@Param("employeeId") Long employeeId);

}
