package com.uptrix.uptrix_backend.service.payroll;

import com.uptrix.uptrix_backend.dto.payroll.SalaryStructureComponentDto;
import com.uptrix.uptrix_backend.dto.payroll.SalaryStructureDto;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.payroll.SalaryStructure;
import com.uptrix.uptrix_backend.entity.payroll.SalaryStructureComponent;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.payroll.SalaryStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public SalaryStructureService(SalaryStructureRepository salaryStructureRepository,
                                  EmployeeRepository employeeRepository,
                                  CompanyRepository companyRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public SalaryStructureDto getCurrentForEmployee(Long employeeId, LocalDate date) {
        var opt = salaryStructureRepository
                .findFirstByEmployeeIdAndEffectiveFromLessThanEqualAndStatusOrderByEffectiveFromDesc(
                        employeeId,
                        date,
                        "ACTIVE"
                );

        return opt.map(this::toDto).orElse(null);
    }

    @Transactional
    public SalaryStructureDto saveOrUpdate(SalaryStructureDto dto) {
        if (dto.getEmployeeId() == null || dto.getCompanyId() == null) {
            throw new IllegalArgumentException("employeeId and companyId are required");
        }

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        SalaryStructure entity;
        if (dto.getId() != null) {
            entity = salaryStructureRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("SalaryStructure not found"));
            entity.getComponents().clear();
        } else {
            entity = new SalaryStructure();
        }

        entity.setEmployee(employee);
        entity.setCompany(company);
        entity.setEffectiveFrom(dto.getEffectiveFrom() != null ? dto.getEffectiveFrom() : LocalDate.now());
        entity.setEffectiveTo(dto.getEffectiveTo());
        entity.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "INR");
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        if (dto.getComponents() != null) {
            for (SalaryStructureComponentDto cDto : dto.getComponents()) {
                SalaryStructureComponent c = new SalaryStructureComponent();
                c.setSalaryStructure(entity);
                c.setComponentCode(cDto.getComponentCode());
                c.setComponentName(cDto.getComponentName());
                c.setComponentType(cDto.getComponentType());
                c.setCalculationType(cDto.getCalculationType());
                c.setAmountValue(cDto.getAmountValue());
                c.setPercentOfBasic(cDto.getPercentOfBasic());
                c.setTaxable(cDto.getTaxable());
                c.setSequenceNo(cDto.getSequenceNo());
                entity.getComponents().add(c);
            }
        }

        SalaryStructure saved = salaryStructureRepository.save(entity);
        return toDto(saved);
    }

    private SalaryStructureDto toDto(SalaryStructure entity) {
        SalaryStructureDto dto = new SalaryStructureDto();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee().getId());
        dto.setCompanyId(entity.getCompany().getId());
        dto.setEffectiveFrom(entity.getEffectiveFrom());
        dto.setEffectiveTo(entity.getEffectiveTo());
        dto.setCurrency(entity.getCurrency());
        dto.setStatus(entity.getStatus());

        dto.setComponents(entity.getComponents().stream().map(c -> {
            SalaryStructureComponentDto cd = new SalaryStructureComponentDto();
            cd.setId(c.getId());
            cd.setComponentCode(c.getComponentCode());
            cd.setComponentName(c.getComponentName());
            cd.setComponentType(c.getComponentType());
            cd.setCalculationType(c.getCalculationType());
            cd.setAmountValue(c.getAmountValue());
            cd.setPercentOfBasic(c.getPercentOfBasic());
            cd.setTaxable(c.getTaxable());
            cd.setSequenceNo(c.getSequenceNo());
            return cd;
        }).collect(Collectors.toList()));

        return dto;
    }
}
