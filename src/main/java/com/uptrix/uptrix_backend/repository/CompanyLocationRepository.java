package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.company.CompanyLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyLocationRepository extends JpaRepository<CompanyLocation, Long> {

    List<CompanyLocation> findByCompanyIdAndActiveTrue(Long companyId);

    Optional<CompanyLocation> findByCompanyIdAndCode(Long companyId, String code);
}
