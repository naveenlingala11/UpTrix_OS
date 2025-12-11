package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByCompanyId(Long companyId);

    List<LeaveRequest> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<LeaveRequest> findByCompanyIdAndEmployeeId(Long companyId, Long employeeId);

    long countByCompanyIdAndStatus(Long companyId, String status);

    // ✅ Corrected method – uses startDate & endDate (NOT fromDate/toDate)
    long countByCompanyIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long companyId,
            String status,
            LocalDate startDate,
            LocalDate endDate
    );

    // ✅ Helper for "on leave today"
    default long countApprovedOverlappingDate(Long companyId, LocalDate date) {
        return countByCompanyIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                companyId,
                "APPROVED",
                date,
                date
        );
    }
    // Leaves that include a specific date (for "on leave today")
    List<LeaveRequest> findByCompanyIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long companyId,
            String status,
            LocalDate dateStart,   // usually: today
            LocalDate dateEnd      // usually: today
    );

    // Recent leaves (for mini-table, paginated)
    Page<LeaveRequest> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);
}
