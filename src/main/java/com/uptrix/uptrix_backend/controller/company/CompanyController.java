package com.uptrix.uptrix_backend.controller.company;

import com.uptrix.uptrix_backend.dto.company.CompanyDto;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.service.company.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Operation(summary = "List companies", description = "Return all companies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Companies returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAll() {
        return ResponseEntity.ok(companyService.findAll());
    }

    @Operation(summary = "Create company", description = "Create a new company profile.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<CompanyDto> create(@RequestBody Company company) {
        CompanyDto saved = companyService.create(company);
        return ResponseEntity.ok(saved);
    }
}
