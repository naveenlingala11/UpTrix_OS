package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByCompanyId(Long companyId);

    Optional<Department> findByCompanyIdAndCode(Long companyId, String code);

    long countByCompanyId(Long companyId);
}
