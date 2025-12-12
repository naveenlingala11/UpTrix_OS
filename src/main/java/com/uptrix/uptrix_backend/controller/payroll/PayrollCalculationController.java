package com.uptrix.uptrix_backend.controller.payroll;

import com.uptrix.uptrix_backend.dto.payroll.PayrollBatchResultDto;
import com.uptrix.uptrix_backend.dto.payroll.PayrollPreviewDto;
import com.uptrix.uptrix_backend.service.payroll.PayrollCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll/calculation")
public class PayrollCalculationController {

    private final PayrollCalculationService payrollCalculationService;

    public PayrollCalculationController(PayrollCalculationService payrollCalculationService) {
        this.payrollCalculationService = payrollCalculationService;
    }

    @Operation(summary = "Preview payroll for an employee", description = "Preview earnings/deductions/net for employee/month",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/preview")
    public ResponseEntity<PayrollPreviewDto> preview(
            @RequestParam Long employeeId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        PayrollPreviewDto dto =
                payrollCalculationService.previewEmployeeMonth(employeeId, year, month);
        return ResponseEntity.ok(dto);
    }

    public static final class GenerateRequest {
        public Long employeeId;
        public Integer year;
        public Integer month;
        public String runType;
    }

    @Operation(summary = "Generate payroll for an employee", description = "Persist payroll earnings and deductions for an employee",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/generate")
    public ResponseEntity<PayrollPreviewDto> generate(@RequestBody GenerateRequest req) {
        if (req.employeeId == null || req.year == null || req.month == null) {
            throw new IllegalArgumentException("employeeId, year and month are required");
        }
        PayrollPreviewDto dto =
                payrollCalculationService.generatePayrollForEmployee(
                        req.employeeId,
                        req.year,
                        req.month,
                        req.runType
                );
        return ResponseEntity.ok(dto);
    }

    public static final class GenerateRunRequest {
        public Long companyId;
        public Integer year;
        public Integer month;
        public String runType;
        public java.util.List<Long> employeeIds;
    }

    @Operation(summary = "Generate payroll run for company", description = "Batch generate payroll for company / selected employees",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/generate-run")
    public ResponseEntity<PayrollBatchResultDto> generateRun(@RequestBody GenerateRunRequest req) {
        if (req.companyId == null || req.year == null || req.month == null) {
            throw new IllegalArgumentException("companyId, year and month are required");
        }
        PayrollBatchResultDto dto =
                payrollCalculationService.generatePayrollForCompany(
                        req.companyId,
                        req.year,
                        req.month,
                        req.runType != null ? req.runType : "REGULAR",
                        req.employeeIds
                );
        return ResponseEntity.ok(dto);
    }

}
