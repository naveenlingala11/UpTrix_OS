package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.EmployeeShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeShiftAssignmentRepository extends JpaRepository<EmployeeShiftAssignment, Long> {

    // =========================
    // BASIC LOOKUPS
    // =========================

    List<EmployeeShiftAssignment> findByEmployee(Employee employee);

    // All assignments for one employee (used by calendar)
    List<EmployeeShiftAssignment> findByEmployeeId(Long employeeId);

    // =========================
    // ACTIVE SHIFT FOR A DATE (WITH EXCEPTION PRIORITY)
    // =========================

    @Query("""
        select esa
        from EmployeeShiftAssignment esa
        where esa.employee.id = :employeeId
          and esa.status = 'ACTIVE'
          and esa.effectiveFrom <= :date
          and (esa.effectiveTo is null or esa.effectiveTo >= :date)
        order by esa.exceptionOverride desc, esa.effectiveFrom desc
    """)
    List<EmployeeShiftAssignment> findActiveAssignmentsForEmployeeOnDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );


    // =========================
    // OVERLAP CONFLICT DETECTION ✅
    // =========================

    @Query("""
        select count(esa)
        from EmployeeShiftAssignment esa
        where esa.employee.id = :employeeId
          and esa.status = 'ACTIVE'
          and esa.effectiveFrom <= :endDate
          and (esa.effectiveTo is null or esa.effectiveTo >= :startDate)
    """)
    long countOverlappingAssignments(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    // =========================
    // CALENDAR / SCHEDULE VIEW ✅
    // =========================

    @Query("""
        select esa
        from EmployeeShiftAssignment esa
        where esa.employee.id = :employeeId
          and esa.status = 'ACTIVE'
          and esa.effectiveFrom <= :endDate
          and (esa.effectiveTo is null or esa.effectiveTo >= :startDate)
        order by esa.effectiveFrom asc
    """)
    List<EmployeeShiftAssignment> findAssignmentsForEmployeeInRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );



    // =========================
    // ✅ ✅ ✅ MISSING METHOD – THIS FIXES YOUR ERROR
    // =========================

    /**
     * Count how many employees have ANY active shift on a given date.
     * Used for dashboards & summaries.
     */
    @Query("""
        select count(distinct esa.employee.id)
        from EmployeeShiftAssignment esa
        where esa.employee.company.id = :companyId
          and esa.status = 'ACTIVE'
          and esa.effectiveFrom <= :date
          and (esa.effectiveTo is null or esa.effectiveTo >= :date)
    """)
    long countEmployeesWithActiveShiftOnDate(
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date
    );


    // =========================
    // NIGHT SHIFT ANALYTICS ✅
    // =========================

    @Query("""
        select count(distinct esa.employee.id)
        from EmployeeShiftAssignment esa
        where esa.employee.company.id = :companyId
          and esa.status = 'ACTIVE'
          and esa.shift.nightShift = true
          and esa.effectiveFrom <= :date
          and (esa.effectiveTo is null or esa.effectiveTo >= :date)
    """)
    long countEmployeesOnNightShiftOnDate(
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date
    );
}
