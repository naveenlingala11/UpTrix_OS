package com.uptrix.uptrix_backend.controller.payroll;

import com.uptrix.uptrix_backend.dto.payroll.NightAllowanceGenerateRequest;
import com.uptrix.uptrix_backend.dto.payroll.NightAllowancePreviewRequest;
import com.uptrix.uptrix_backend.dto.payroll.NightAllowancePreviewResponse;
import com.uptrix.uptrix_backend.dto.payroll.PayrollEarningDto;
import com.uptrix.uptrix_backend.entity.payroll.PayrollEarning;
import com.uptrix.uptrix_backend.service.payroll.NightAllowanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payroll/night-allowance")
public class NightAllowanceController {

    private final NightAllowanceService nightAllowanceService;

    public NightAllowanceController(NightAllowanceService nightAllowanceService) {
        this.nightAllowanceService = nightAllowanceService;
    }

    @Operation(summary = "Preview night allowance for employee", description = "Preview calculated night allowance for employee/month",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preview returned"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/preview")
    public ResponseEntity<NightAllowancePreviewResponse> preview(
            @RequestParam Long employeeId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        NightAllowancePreviewRequest req = new NightAllowancePreviewRequest();
        req.setEmployeeId(employeeId);
        req.setYear(year);
        req.setMonth(month);

        NightAllowancePreviewResponse resp = nightAllowanceService.preview(req);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Generate night allowance earning", description = "Persist night allowance earning for payroll")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Earning generated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/generate")
    public ResponseEntity<PayrollEarningDto> generate(@RequestBody NightAllowanceGenerateRequest request) {
        PayrollEarning earning = nightAllowanceService.generateEarning(request);
        PayrollEarningDto dto = nightAllowanceService.toDto(earning);
        return ResponseEntity.ok(dto);
    }
}
