package com.uptrix.uptrix_backend.repository;

import com.uptrix.uptrix_backend.entity.platform.TenantFeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantFeatureFlagRepository extends JpaRepository<TenantFeatureFlag, Long> {

    List<TenantFeatureFlag> findByCompanyId(Long companyId);
}
