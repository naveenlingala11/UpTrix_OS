package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByCompanyId(Long companyId);

    Page<Employee> findByCompanyId(Long companyId, Pageable pageable);

    List<Employee> findByCompanyIdAndDepartmentIdAndStatus(Long companyId, Long departmentId, String status);

    Optional<Employee> findByCompanyIdAndEmployeeCode(Long companyId, String employeeCode);

    Optional<Employee> findByUser(User user);

    long countByCompanyIdAndDateOfJoiningAfter(Long companyId, LocalDate date);

    long countByCompanyIdAndStatusAndUpdatedAtAfter(
            Long companyId,
            String status,
            LocalDateTime updatedAt
    );

    // ✅ FIXED: use workEmail + companyId
    Optional<Employee> findFirstByCompanyIdAndWorkEmail(Long companyId, String workEmail);

    // Used by helpdesk to validate employeeId
    Optional<Employee> findByIdAndCompanyId(Long id, Long companyId);

    @Query("select e.id from Employee e where e.manager.id = :managerId")
    List<Long> findDirectReportIds(@Param("managerId") Long managerId);

    // Manager + team
    List<Employee> findByCompanyIdAndManager_Id(Long companyId, Long managerId);
}
