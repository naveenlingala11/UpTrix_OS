package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.payroll.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    List<SalaryStructure> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);

    Optional<SalaryStructure> findFirstByEmployeeIdAndEffectiveFromLessThanEqualAndStatusOrderByEffectiveFromDesc(
            Long employeeId,
            LocalDate date,
            String status
    );
}
