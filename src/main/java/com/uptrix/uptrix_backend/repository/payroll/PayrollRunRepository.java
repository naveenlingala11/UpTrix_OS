package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.entity.payroll.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    List<PayrollRun> findByCompanyIdOrderByYearDescMonthDesc(Long companyId);

    Optional<PayrollRun> findByCompanyIdAndYearAndMonth(Long companyId, Integer year, Integer month);


    @Query("SELECT p FROM PayrollRun p WHERE p.id IN :ids")
    List<PayrollRun> findByPayrollRunIdIn(@Param("ids") List<Long> ids);
}
