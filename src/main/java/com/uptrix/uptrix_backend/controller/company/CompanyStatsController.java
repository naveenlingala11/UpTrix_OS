package com.uptrix.uptrix_backend.controller.company;

import com.uptrix.uptrix_backend.dto.company.CompanyStatsDto;
import com.uptrix.uptrix_backend.service.company.CompanyStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/stats")
public class CompanyStatsController {

    private final CompanyStatsService statsService;

    public CompanyStatsController(CompanyStatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(summary = "Get company stats", description = "Get statistics for a company.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("@companySecurity.canAccessCompany(authentication, #companyId)")
    public ResponseEntity<CompanyStatsDto> getStats(@PathVariable Long companyId) {
        return ResponseEntity.ok(statsService.getStats(companyId));
    }
}
