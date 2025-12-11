package com.uptrix.uptrix_backend.controller.company;

import com.uptrix.uptrix_backend.dto.company.CompanySettingsDto;
import com.uptrix.uptrix_backend.service.company.CompanySettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/settings")
public class CompanySettingsController {

    private final CompanySettingsService settingsService;

    public CompanySettingsController(CompanySettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Operation(summary = "Get company settings", description = "Get settings for a company.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settings returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<CompanySettingsDto> get(@PathVariable Long companyId) {
        return ResponseEntity.ok(settingsService.getForCompany(companyId));
    }

    @Operation(summary = "Update company settings", description = "Update settings for a company.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping
    public ResponseEntity<CompanySettingsDto> update(@PathVariable Long companyId,
                                                     @RequestBody CompanySettingsDto dto) {
        return ResponseEntity.ok(settingsService.updateForCompany(companyId, dto));
    }
}
