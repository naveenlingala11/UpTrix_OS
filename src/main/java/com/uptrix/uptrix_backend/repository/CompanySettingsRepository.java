package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.company.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {

    Optional<CompanySettings> findByCompanyId(Long companyId);
}
