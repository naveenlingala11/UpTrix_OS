package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.payroll.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Long> {

    List<Bonus> findByCompanyIdAndStatusInOrderByCreatedAtDesc(Long companyId, List<String> statuses);

    List<Bonus> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
