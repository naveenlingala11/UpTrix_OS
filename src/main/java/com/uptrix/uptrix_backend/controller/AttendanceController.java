package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.AttendanceDto;
import com.uptrix.uptrix_backend.service.AttendanceService;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/attendance")
@Tag(name = "Attendance", description = "Endpoints for employee attendance & export")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Operation(summary = "Check-in employee",
            description = "Marks an employee as checked-in for the day. If already checked in, returns existing attendance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-in recorded",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping("/{employeeId}/check-in")
    public ResponseEntity<AttendanceDto> checkIn(@PathVariable Long companyId,
                                                 @PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkIn(companyId, employeeId));
    }

    @Operation(summary = "Check-out employee",
            description = "Marks an employee as checked-out for the day.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check-out recorded",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping("/{employeeId}/check-out")
    public ResponseEntity<AttendanceDto> checkOut(@PathVariable Long companyId,
                                                  @PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkOut(companyId, employeeId));
    }

    @Operation(summary = "List today's attendance for company",
            description = "Returns list of attendance records for the current day for the given company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AttendanceDto.class))))
    })
    @GetMapping("/today")
    public ResponseEntity<List<AttendanceDto>> today(@PathVariable Long companyId) {
        return ResponseEntity.ok(attendanceService.getTodayAttendance(companyId));
    }

    @Operation(summary = "Export today's attendance CSV",
            description = "Streams a CSV file containing today's attendance for the company.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV streamed"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/export")
    public void exportCsv(@PathVariable Long companyId, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=attendance.csv");

        List<AttendanceDto> list = attendanceService.getTodayAttendance(companyId);

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Date,Employee Code,Employee Name,Status,Check In,Check Out");

            for (AttendanceDto a : list) {
                String line = String.join(",",
                        safe(a.getDate() != null ? a.getDate().toString() : ""),
                        safe(a.getEmployeeCode()),
                        safe(a.getEmployeeName()),
                        safe(a.getStatus()),
                        safe(a.getCheckInTime() != null ? a.getCheckInTime().toString() : ""),
                        safe(a.getCheckOutTime() != null ? a.getCheckOutTime().toString() : "")
                );
                writer.println(line);
            }
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}
