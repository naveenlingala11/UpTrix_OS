package com.uptrix.uptrix_backend.controller.shift;

import com.uptrix.uptrix_backend.dto.shift.EmployeeShiftAssignmentDto;
import com.uptrix.uptrix_backend.service.shift.EmployeeShiftAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee-shifts")
public class EmployeeShiftAssignmentController {

    private final EmployeeShiftAssignmentService assignmentService;

    public EmployeeShiftAssignmentController(EmployeeShiftAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @Operation(summary = "Get shift assignments for employee", description = "Fetch assignments as DTO for calendar",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeShiftAssignmentDto>> getAssignmentsForEmployee(
            @PathVariable Long employeeId
    ) {
        List<EmployeeShiftAssignmentDto> list =
                assignmentService.getAssignmentsForEmployee(employeeId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Assign shift to employee", description = "Assign a shift via drag & drop or API",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<EmployeeShiftAssignmentDto> assignShift(
            @RequestParam Long employeeId,
            @RequestParam Long shiftId,
            @RequestParam String effectiveFrom,
            @RequestParam(required = false) String effectiveTo,
            @RequestParam(required = false, defaultValue = "false") Boolean exceptionOverride
    ) {
        LocalDate from = LocalDate.parse(effectiveFrom);
        LocalDate to = (effectiveTo != null ? LocalDate.parse(effectiveTo) : from);

        EmployeeShiftAssignmentDto dto =
                assignmentService.assignShiftDto(employeeId, shiftId, from, to, exceptionOverride);

        return ResponseEntity.ok(dto);
    }
}
