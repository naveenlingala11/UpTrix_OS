package com.uptrix.uptrix_backend.controller.attendance;

import com.uptrix.uptrix_backend.dto.attendance.AttendanceDayDto;
import com.uptrix.uptrix_backend.service.attendance.AttendanceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceQueryController {

    private final AttendanceQueryService attendanceQueryService;

    public AttendanceQueryController(AttendanceQueryService attendanceQueryService) {
        this.attendanceQueryService = attendanceQueryService;
    }

    @Operation(summary = "Get monthly attendance for an employee",
            description = "Returns daily attendance summary (worked minutes, check-in/out) for the requested month.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance list returned"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/employee/{employeeId}/month")
    public ResponseEntity<List<AttendanceDayDto>> getMonthForEmployee(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<AttendanceDayDto> list =
                attendanceQueryService.getMonthForEmployee(employeeId, year, month);
        return ResponseEntity.ok(list);
    }
}
