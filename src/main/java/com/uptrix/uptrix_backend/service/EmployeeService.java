package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.employee.EmployeeCreateRequest;
import com.uptrix.uptrix_backend.dto.employee.EmployeeResponseDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.entity.company.CompanyLocation;
import com.uptrix.uptrix_backend.entity.Department;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.repository.CompanyLocationRepository;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.DepartmentRepository;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyLocationRepository companyLocationRepository;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepository employeeRepository,
                           CompanyRepository companyRepository,
                           DepartmentRepository departmentRepository,
                           CompanyLocationRepository companyLocationRepository,
                           AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.companyLocationRepository = companyLocationRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    @CacheEvict(cacheNames = "companyStats", key = "#companyId")
    public EmployeeResponseDto create(Long companyId, EmployeeCreateRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));

        if (employeeRepository.findByCompanyIdAndEmployeeCode(companyId, request.getEmployeeCode()).isPresent()) {
            throw new IllegalArgumentException("Employee code already exists in this company");
        }

        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setWorkEmail(request.getWorkEmail());
        employee.setEmploymentType(request.getEmploymentType());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setStatus("ACTIVE");

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid department id"));
            employee.setDepartment(department);
        }

        if (request.getLocationId() != null) {
            CompanyLocation location = companyLocationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid location id"));
            employee.setLocation(location);
        }

        Employee saved = employeeRepository.save(employee);

        String codeOrId = saved.getEmployeeCode() != null
                ? saved.getEmployeeCode()
                : String.valueOf(saved.getId());

        auditLogService.log(
                "EMPLOYEE_CREATED",
                "EMPLOYEE",
                saved.getId(),
                "Employee " + codeOrId + " created"
        );

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> listByCompany(Long companyId) {
        return employeeRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> listByCompanyPaged(Long companyId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return employeeRepository.findByCompanyId(companyId, pageable)
                .map(this::toDto);
    }

    private EmployeeResponseDto toDto(Employee e) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(e.getId());
        dto.setEmployeeCode(e.getEmployeeCode());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setFullName(
                (e.getFirstName() != null ? e.getFirstName() : "") +
                        (e.getLastName() != null ? " " + e.getLastName() : "")
        );
        dto.setWorkEmail(e.getWorkEmail());
        dto.setEmploymentType(e.getEmploymentType());
        dto.setStatus(e.getStatus());
        dto.setDateOfJoining(e.getDateOfJoining());

        if (e.getDepartment() != null) {
            dto.setDepartmentName(e.getDepartment().getName());
        }

        if (e.getLocation() != null) {
            dto.setLocationId(e.getLocation().getId());
            dto.setLocationName(e.getLocation().getName());
        }

        return dto;
    }

    @Transactional
    public int bulkUploadFromExcel(Long companyId, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int imported = 0;

            boolean first = true;
            for (Row row : sheet) {
                if (first) {
                    first = false;
                    continue;
                }
                if (row == null) continue;

                String employeeCode = getString(row.getCell(0));
                String firstName = getString(row.getCell(1));
                String lastName = getString(row.getCell(2));
                String email = getString(row.getCell(3));
                String deptCode = getString(row.getCell(4));

                if (employeeCode.isBlank() || firstName.isBlank()) {
                    continue;
                }

                Department dept = departmentRepository
                        .findByCompanyIdAndCode(companyId, deptCode)
                        .orElse(null);

                Employee emp = new Employee();
                emp.setEmployeeCode(employeeCode);
                emp.setFirstName(firstName);
                emp.setLastName(lastName);
                emp.setWorkEmail(email);
                emp.setCompany(companyRepository.getReferenceById(companyId));
                emp.setDepartment(dept);
                emp.setStatus("ACTIVE");

                employeeRepository.save(emp);
                imported++;
            }

            return imported;
        }
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

}
