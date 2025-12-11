package com.uptrix.uptrix_backend.controller.attendance;

import com.uptrix.uptrix_backend.dto.attendance.GeoAttendancePunchRequest;
import com.uptrix.uptrix_backend.entity.Attendance;
import com.uptrix.uptrix_backend.service.attendance.GeoAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
public class GeoAttendanceController {

    private final GeoAttendanceService geoAttendanceService;

    public GeoAttendanceController(GeoAttendanceService geoAttendanceService) {
        this.geoAttendanceService = geoAttendanceService;
    }

    @Operation(summary = "Geo-fenced punch (check-in/out)",
            description = "Record an attendance punch with geo coordinates. Returns saved attendance record.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Punch recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/geo-punch")
    public ResponseEntity<Attendance> geoPunch(@RequestBody GeoAttendancePunchRequest request) {
        Attendance saved = geoAttendanceService.punch(request);
        return ResponseEntity.ok(saved);
    }
}
