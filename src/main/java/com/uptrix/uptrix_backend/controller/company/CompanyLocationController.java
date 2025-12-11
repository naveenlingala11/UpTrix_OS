package com.uptrix.uptrix_backend.controller.company;

import com.uptrix.uptrix_backend.dto.company.CompanyLocationDto;
import com.uptrix.uptrix_backend.service.company.CompanyLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/locations")
public class CompanyLocationController {

    private final CompanyLocationService locationService;

    public CompanyLocationController(CompanyLocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "List company locations", description = "Get active locations for a company.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<CompanyLocationDto>> getLocations(@PathVariable Long companyId) {
        return ResponseEntity.ok(locationService.getActiveLocations(companyId));
    }

    @Operation(summary = "Create location", description = "Create a new company location.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<CompanyLocationDto> create(@PathVariable Long companyId,
                                                     @RequestBody CompanyLocationDto dto) {
        return ResponseEntity.ok(locationService.createLocation(companyId, dto));
    }

    @Operation(summary = "Update location", description = "Update an existing company location.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{locationId}")
    public ResponseEntity<CompanyLocationDto> update(@PathVariable Long companyId,
                                                     @PathVariable Long locationId,
                                                     @RequestBody CompanyLocationDto dto) {
        return ResponseEntity.ok(locationService.updateLocation(locationId, dto));
    }
}
