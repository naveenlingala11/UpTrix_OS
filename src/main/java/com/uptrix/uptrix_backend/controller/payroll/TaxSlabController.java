package com.uptrix.uptrix_backend.controller.payroll;

import com.uptrix.uptrix_backend.dto.payroll.TaxSlabDto;
import com.uptrix.uptrix_backend.service.payroll.TaxSlabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll/tax-slabs")
public class TaxSlabController {

    private final TaxSlabService taxSlabService;

    public TaxSlabController(TaxSlabService taxSlabService) {
        this.taxSlabService = taxSlabService;
    }

    @Operation(summary = "List tax slabs for company", description = "List active tax slabs for a company",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<List<TaxSlabDto>> list(
            @RequestParam Long companyId
    ) {
        List<TaxSlabDto> list = taxSlabService.listForCompany(companyId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Save tax slab", description = "Create or update a tax slab",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<TaxSlabDto> save(@RequestBody TaxSlabDto dto) {
        TaxSlabDto saved = taxSlabService.save(dto);
        return ResponseEntity.ok(saved);
    }
}
