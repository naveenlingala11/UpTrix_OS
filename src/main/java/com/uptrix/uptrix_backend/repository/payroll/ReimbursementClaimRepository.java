package com.uptrix.uptrix_backend.repository.payroll;

import com.uptrix.uptrix_backend.entity.payroll.ReimbursementClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementClaimRepository extends JpaRepository<ReimbursementClaim, Long> {

    List<ReimbursementClaim> findByCompanyIdAndStatusInOrderBySubmittedAtDesc(Long companyId, List<String> statuses);

    List<ReimbursementClaim> findByEmployeeIdOrderBySubmittedAtDesc(Long employeeId);

}
