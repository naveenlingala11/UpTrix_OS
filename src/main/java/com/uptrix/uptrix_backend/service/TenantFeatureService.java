package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.dashboard.TenantFeaturesDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.platform.TenantFeatureFlag;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.TenantFeatureFlagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantFeatureService {

    private final CompanyRepository companyRepository;
    private final TenantFeatureFlagRepository tenantFeatureFlagRepository;

    public TenantFeatureService(
            CompanyRepository companyRepository,
            TenantFeatureFlagRepository tenantFeatureFlagRepository
    ) {
        this.companyRepository = companyRepository;
        this.tenantFeatureFlagRepository = tenantFeatureFlagRepository;
    }

    @Transactional(readOnly = true)
    public TenantFeaturesDto getFeaturesForTenant(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        List<TenantFeatureFlag> flags = tenantFeatureFlagRepository.findByCompanyId(companyId);
        Map<String, Boolean> map = new HashMap<>();
        for (TenantFeatureFlag flag : flags) {
            map.put(flag.getFeatureKey(), flag.isEnabled());
        }

        TenantFeaturesDto dto = new TenantFeaturesDto();
        dto.setCompanyId(company.getId());

        // Defaults = true if not explicitly stored
        dto.setAttendanceEnabled(map.getOrDefault("ATTENDANCE", true));
        dto.setLeavesEnabled(map.getOrDefault("LEAVES", true));
        dto.setShiftsEnabled(map.getOrDefault("SHIFTS", true));
        dto.setHelpdeskEnabled(map.getOrDefault("HELP_DESK", true));
        dto.setProjectsEnabled(map.getOrDefault("PROJECTS", true));
        dto.setCrmEnabled(map.getOrDefault("CRM", true));

        return dto;
    }

    @Transactional
    public TenantFeaturesDto updateFeaturesForTenant(Long companyId, TenantFeaturesDto request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        List<TenantFeatureFlag> existing = tenantFeatureFlagRepository.findByCompanyId(companyId);
        Map<String, TenantFeatureFlag> existingMap = new HashMap<>();
        for (TenantFeatureFlag flag : existing) {
            existingMap.put(flag.getFeatureKey(), flag);
        }

        upsertFlag(company, existingMap, "ATTENDANCE", request.isAttendanceEnabled());
        upsertFlag(company, existingMap, "LEAVES", request.isLeavesEnabled());
        upsertFlag(company, existingMap, "SHIFTS", request.isShiftsEnabled());
        upsertFlag(company, existingMap, "HELP_DESK", request.isHelpdeskEnabled());
        upsertFlag(company, existingMap, "PROJECTS", request.isProjectsEnabled());
        upsertFlag(company, existingMap, "CRM", request.isCrmEnabled());

        return getFeaturesForTenant(companyId);
    }

    private void upsertFlag(Company company,
                            Map<String, TenantFeatureFlag> existingMap,
                            String key,
                            boolean enabled) {

        TenantFeatureFlag flag = existingMap.get(key);
        if (flag == null) {
            flag = new TenantFeatureFlag();
            flag.setCompany(company);
            flag.setFeatureKey(key);
        }
        flag.setEnabled(enabled);
        tenantFeatureFlagRepository.save(flag);
    }
}
