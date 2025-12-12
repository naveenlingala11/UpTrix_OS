package com.uptrix.uptrix_backend.service.company;

import com.uptrix.uptrix_backend.dto.company.CompanyLocationDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.company.CompanyLocation;
import com.uptrix.uptrix_backend.repository.CompanyLocationRepository;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyLocationService {

    private final CompanyLocationRepository locationRepository;
    private final CompanyRepository companyRepository;

    public CompanyLocationService(CompanyLocationRepository locationRepository,
                                  CompanyRepository companyRepository) {
        this.locationRepository = locationRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyLocationDto> getActiveLocations(Long companyId) {
        return locationRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CompanyLocationDto createLocation(Long companyId, CompanyLocationDto dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        CompanyLocation loc = new CompanyLocation();
        loc.setCompany(company);
        loc.setCode(dto.getCode());
        loc.setName(dto.getName());
        loc.setAddressLine1(dto.getAddressLine1());
        loc.setAddressLine2(dto.getAddressLine2());
        loc.setCity(dto.getCity());
        loc.setState(dto.getState());
        loc.setPincode(dto.getPincode());
        loc.setCountry(dto.getCountry());
        loc.setActive(true);

        CompanyLocation saved = locationRepository.save(loc);
        return toDto(saved);
    }

    @Transactional
    public CompanyLocationDto updateLocation(Long locationId, CompanyLocationDto dto) {
        CompanyLocation loc = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        loc.setName(dto.getName());
        loc.setAddressLine1(dto.getAddressLine1());
        loc.setAddressLine2(dto.getAddressLine2());
        loc.setCity(dto.getCity());
        loc.setState(dto.getState());
        loc.setPincode(dto.getPincode());
        loc.setCountry(dto.getCountry());
        loc.setActive(dto.isActive());

        CompanyLocation saved = locationRepository.save(loc);
        return toDto(saved);
    }

    private CompanyLocationDto toDto(CompanyLocation loc) {
        CompanyLocationDto dto = new CompanyLocationDto();
        dto.setId(loc.getId());
        dto.setCompanyId(loc.getCompany().getId());
        dto.setCode(loc.getCode());
        dto.setName(loc.getName());
        dto.setAddressLine1(loc.getAddressLine1());
        dto.setAddressLine2(loc.getAddressLine2());
        dto.setCity(loc.getCity());
        dto.setState(loc.getState());
        dto.setPincode(loc.getPincode());
        dto.setCountry(loc.getCountry());
        dto.setActive(loc.isActive());
        return dto;
    }
}
