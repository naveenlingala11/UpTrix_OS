package com.uptrix.uptrix_backend.service.company;

import com.uptrix.uptrix_backend.dto.company.CompanySettingsDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.company.CompanySettings;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.CompanySettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanySettingsService {

    private final CompanySettingsRepository settingsRepository;
    private final CompanyRepository companyRepository;

    public CompanySettingsService(CompanySettingsRepository settingsRepository,
                                  CompanyRepository companyRepository) {
        this.settingsRepository = settingsRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public CompanySettingsDto getForCompany(Long companyId) {
        CompanySettings settings = settingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> createDefaultSettings(companyId));

        return toDto(settings);
    }

    @Transactional
    public CompanySettingsDto updateForCompany(Long companyId, CompanySettingsDto dto) {
        CompanySettings settings = settingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> createDefaultSettings(companyId));

        settings.setWorkdayStart(dto.getWorkdayStart());
        settings.setWorkdayEnd(dto.getWorkdayEnd());
        settings.setDefaultLeaveTypes(dto.getDefaultLeaveTypes());
        settings.setTimezoneLabel(dto.getTimezoneLabel());

        CompanySettings saved = settingsRepository.save(settings);
        return toDto(saved);
    }

    private CompanySettings createDefaultSettings(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        CompanySettings settings = new CompanySettings();
        settings.setCompany(company);
        settings.setWorkdayStart("09:00");
        settings.setWorkdayEnd("18:00");
        settings.setDefaultLeaveTypes("PLANNED,SICK,CASUAL");
        settings.setTimezoneLabel("Asia/Kolkata");

        return settingsRepository.save(settings);
    }

    private CompanySettingsDto toDto(CompanySettings s) {
        CompanySettingsDto dto = new CompanySettingsDto();
        dto.setCompanyId(s.getCompany().getId());
        dto.setWorkdayStart(s.getWorkdayStart());
        dto.setWorkdayEnd(s.getWorkdayEnd());
        dto.setDefaultLeaveTypes(s.getDefaultLeaveTypes());
        dto.setTimezoneLabel(s.getTimezoneLabel());
        return dto;
    }
}
