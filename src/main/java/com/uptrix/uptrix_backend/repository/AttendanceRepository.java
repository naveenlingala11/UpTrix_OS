package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByCompanyIdAndEmployeeIdAndDate(Long companyId, Long employeeId, LocalDate date);

    List<Attendance> findByCompanyIdAndDate(Long companyId, LocalDate date);

    long countByCompanyIdAndDate(Long companyId, LocalDate date);

    Page<Attendance> findByCompanyIdAndEmployeeIdOrderByDateDesc(
            Long companyId,
            Long employeeId,
            Pageable pageable
    );

    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);

    List<Attendance> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end);

}
