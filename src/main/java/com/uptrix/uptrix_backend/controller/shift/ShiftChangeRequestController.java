package com.uptrix.uptrix_backend.controller.shift;

import com.uptrix.uptrix_backend.dto.shift.ShiftChangeDecisionDto;
import com.uptrix.uptrix_backend.dto.shift.ShiftChangeRequestDto;
import com.uptrix.uptrix_backend.entity.ShiftChangeRequest;
import com.uptrix.uptrix_backend.service.shift.EmployeeShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shift-change-requests")
public class ShiftChangeRequestController {

    private final EmployeeShiftService employeeShiftService;

    public ShiftChangeRequestController(EmployeeShiftService employeeShiftService) {
        this.employeeShiftService = employeeShiftService;
    }

    @Operation(summary = "Create shift change request", description = "Employee requests shift change",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ShiftChangeRequest> create(@RequestBody ShiftChangeRequestDto dto) {
        return ResponseEntity.ok(employeeShiftService.createShiftChangeRequest(dto));
    }

    @Operation(summary = "Decide shift change request", description = "Manager/HR approve or reject request",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/decision")
    public ResponseEntity<ShiftChangeRequest> decide(@PathVariable Long id,
                                                     @RequestBody ShiftChangeDecisionDto decisionDto) {
        return ResponseEntity.ok(employeeShiftService.decideShiftChangeRequest(id, decisionDto));
    }

    @Operation(summary = "Get requests for employee", description = "List shift change requests for employee",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftChangeRequest>> getForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeShiftService.getShiftChangeRequestsForEmployee(employeeId));
    }

    @Operation(summary = "Get pending requests", description = "List pending requests (for approvers)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/pending")
    public ResponseEntity<List<ShiftChangeRequest>> getPending() {
        return ResponseEntity.ok(employeeShiftService.getPendingShiftChangeRequests());
    }
}
