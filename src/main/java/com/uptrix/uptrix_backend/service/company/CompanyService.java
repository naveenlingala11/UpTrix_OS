package com.uptrix.uptrix_backend.service.company;

import com.uptrix.uptrix_backend.dto.company.CompanyDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> findAll() {
        return companyRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CompanyDto create(Company company) {

        if (companyRepository.existsByName(company.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Company with this name already exists"
            );
        }

        if (company.getSubdomain() != null &&
                !company.getSubdomain().isBlank() &&
                companyRepository.existsBySubdomain(company.getSubdomain())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This subdomain is already in use"
            );
        }

        Company saved = companyRepository.save(company);
        return toDto(saved);
    }

    private CompanyDto toDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setLegalName(company.getLegalName());
        dto.setSubdomain(company.getSubdomain());
        dto.setStatus(company.getStatus());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
}
