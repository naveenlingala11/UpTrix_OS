package com.uptrix.uptrix_backend.controller.shift;

import com.uptrix.uptrix_backend.dto.shift.ShiftGeoUpdateRequest;
import com.uptrix.uptrix_backend.entity.Shift;
import com.uptrix.uptrix_backend.service.shift.ShiftGeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shifts")
public class ShiftGeoController {

    private final ShiftGeoService shiftGeoService;

    public ShiftGeoController(ShiftGeoService shiftGeoService) {
        this.shiftGeoService = shiftGeoService;
    }

    @Operation(summary = "Update shift geofence", description = "Update shift geo center and radius",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id}/geo")
    public ResponseEntity<Shift> updateGeo(
            @PathVariable Long id,
            @RequestBody ShiftGeoUpdateRequest request
    ) {
        Shift updated = shiftGeoService.updateGeo(id, request);
        return ResponseEntity.ok(updated);
    }
}
