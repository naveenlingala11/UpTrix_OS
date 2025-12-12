package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.DepartmentDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.Department;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             CompanyRepository companyRepository) {
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> listByCompany(Long companyId) {
        return departmentRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public DepartmentDto create(Long companyId, DepartmentDto dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        Department department = new Department();
        department.setCompany(company);
        department.setName(dto.getName());
        department.setCode(dto.getCode());
        department.setStatus("ACTIVE");

        Department saved = departmentRepository.save(department);
        return toDto(saved);
    }

    private DepartmentDto toDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setCode(department.getCode());
        dto.setStatus(department.getStatus());
        dto.setCompanyId(department.getCompany().getId());
        return dto;
    }
}
