package com.uptrix.uptrix_backend.controller.shift;

import com.uptrix.uptrix_backend.dto.shift.ShiftRequest;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.service.shift.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @Operation(summary = "List shifts", description = "Return all shift definitions",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<List<Shift>> getAll() {
        return ResponseEntity.ok(shiftService.getAll());
    }

    @Operation(summary = "List active shifts", description = "Return active shifts only",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/active")
    public ResponseEntity<List<Shift>> getAllActive() {
        return ResponseEntity.ok(shiftService.getAllActive());
    }

    @Operation(summary = "Create shift", description = "Create a new shift definition",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<Shift> create(@RequestBody ShiftRequest request) {
        Shift created = shiftService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Update shift status", description = "Activate or deactivate shift",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/status")
    public ResponseEntity<Shift> updateStatus(@PathVariable Long id,
                                              @RequestParam String status) {
        Shift updated = shiftService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
