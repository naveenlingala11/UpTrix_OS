package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.DepartmentDto;
import com.uptrix.uptrix_backend.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/departments")
@Tag(name = "Departments", description = "Manage company departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "List departments", description = "List departments for a company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departments returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DepartmentDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<DepartmentDto>> list(@PathVariable Long companyId) {
        return ResponseEntity.ok(departmentService.listByCompany(companyId));
    }

    @Operation(summary = "Create department", description = "Create a new department for the company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department created",
                    content = @Content(schema = @Schema(implementation = DepartmentDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<DepartmentDto> create(@PathVariable Long companyId,
                                                @RequestBody DepartmentDto dto) {
        DepartmentDto created = departmentService.create(companyId, dto);
        return ResponseEntity.ok(created);
    }
}
