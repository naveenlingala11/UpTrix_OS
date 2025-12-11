package com.uptrix.uptrix_backend.controller.payroll;

import com.uptrix.uptrix_backend.dto.payroll.SalaryStructureDto;
import com.uptrix.uptrix_backend.service.payroll.SalaryStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/payroll/salary-structure")
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    public SalaryStructureController(SalaryStructureService salaryStructureService) {
        this.salaryStructureService = salaryStructureService;
    }

    @Operation(summary = "Get current salary structure for employee", description = "Fetch active salary structure for employee as of date",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/employee/{employeeId}/current")
    public ResponseEntity<SalaryStructureDto> getCurrentForEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) String date
    ) {
        LocalDate refDate = (date != null ? LocalDate.parse(date) : LocalDate.now());
        SalaryStructureDto dto = salaryStructureService.getCurrentForEmployee(employeeId, refDate);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Save or update salary structure", description = "Create or update salary structure for an employee",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<SalaryStructureDto> saveOrUpdate(@RequestBody SalaryStructureDto dto) {
        SalaryStructureDto saved = salaryStructureService.saveOrUpdate(dto);
        return ResponseEntity.ok(saved);
    }
}
