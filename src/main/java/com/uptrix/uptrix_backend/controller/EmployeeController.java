package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.employee.EmployeeCreateRequest;
import com.uptrix.uptrix_backend.dto.employee.EmployeeResponseDto;
import com.uptrix.uptrix_backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/employees")
@Tag(name = "Employees", description = "CRUD and import endpoints for employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Create employee", description = "Create a new employee in the company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee created",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@PathVariable Long companyId,
                                                      @RequestBody EmployeeCreateRequest request) {
        EmployeeResponseDto dto = employeeService.create(companyId, request);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "List employees", description = "List all employees for the company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employees returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmployeeResponseDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> list(@PathVariable Long companyId) {
        return ResponseEntity.ok(employeeService.listByCompany(companyId));
    }

    @Operation(summary = "List employees (paged)",
            description = "Get employees with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paged employees",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/paged")
    public ResponseEntity<Page<EmployeeResponseDto>> listPaged(@PathVariable Long companyId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(employeeService.listByCompanyPaged(companyId, page, size));
    }

    @Operation(summary = "Bulk upload employees via Excel",
            description = "Upload an Excel file to import multiple employees")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Import successful"),
            @ApiResponse(responseCode = "400", description = "Import failed")
    })
    @PostMapping("/bulk-upload")
    public ResponseEntity<String> bulkUpload(@PathVariable Long companyId,
                                             @RequestParam("file") MultipartFile file) {
        try {
            int count = employeeService.bulkUploadFromExcel(companyId, file);
            return ResponseEntity.ok("Imported " + count + " employees");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to import: " + e.getMessage());
        }
    }
}
