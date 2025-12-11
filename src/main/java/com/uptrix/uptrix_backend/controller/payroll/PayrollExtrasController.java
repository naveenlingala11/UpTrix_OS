package com.uptrix.uptrix_backend.controller.payroll;

import com.uptrix.uptrix_backend.dto.payroll.BonusDto;
import com.uptrix.uptrix_backend.dto.payroll.ClaimDto;
import com.uptrix.uptrix_backend.service.payroll.PayrollExtrasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll/extras")
public class PayrollExtrasController {

    private final PayrollExtrasService extrasService;

    public PayrollExtrasController(PayrollExtrasService extrasService) {
        this.extrasService = extrasService;
    }

    @Operation(summary = "Submit reimbursement claim", description = "Submit a reimbursement claim to be reviewed/approved",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/claim")
    public ResponseEntity<ClaimDto> submitClaim(@RequestBody ClaimDto dto) {
        return ResponseEntity.ok(extrasService.submitClaim(dto));
    }

    @Operation(summary = "Submit bonus", description = "Submit a bonus for employee",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/bonus")
    public ResponseEntity<BonusDto> submitBonus(@RequestBody BonusDto dto) {
        return ResponseEntity.ok(extrasService.submitBonus(dto));
    }

    @Operation(summary = "List claims", description = "List reimbursement claims for a company",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/claims")
    public ResponseEntity<List<ClaimDto>> listClaims(@RequestParam Long companyId) {
        return ResponseEntity.ok(extrasService.listClaims(companyId));
    }

    @Operation(summary = "List bonuses", description = "List bonuses for a company",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/bonuses")
    public ResponseEntity<List<BonusDto>> listBonuses(@RequestParam Long companyId) {
        return ResponseEntity.ok(extrasService.listBonuses(companyId));
    }

    @Operation(summary = "Approve claim", description = "Approve a reimbursement claim",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/claims/{id}/approve")
    public ResponseEntity<Void> approveClaim(@PathVariable Long id, @RequestParam Long approverId) {
        extrasService.approveClaim(id, approverId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Approve bonus", description = "Approve a bonus",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/bonuses/{id}/approve")
    public ResponseEntity<Void> approveBonus(@PathVariable Long id, @RequestParam Long approverId) {
        extrasService.approveBonus(id, approverId);
        return ResponseEntity.ok().build();
    }
}
