package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.leave.LeaveRequestCreateDto;
import com.uptrix.uptrix_backend.dto.leave.LeaveRequestDto;
import com.uptrix.uptrix_backend.dto.leave.LeaveStatusUpdateDto;
import com.uptrix.uptrix_backend.service.LeaveRequestService;
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
@RequestMapping("/api/companies/{companyId}/leaves")
@Tag(name = "Leaves", description = "Employee leave requests and exports")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @Operation(summary = "Create leave request", description = "Employee creates a leave request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave created",
                    content = @Content(schema = @Schema(implementation = LeaveRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<LeaveRequestDto> create(@PathVariable Long companyId,
                                                  @RequestBody LeaveRequestCreateDto request) {
        return ResponseEntity.ok(leaveRequestService.createLeave(companyId, request));
    }

    @Operation(summary = "List leave requests", description = "List leave requests for company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeaveRequestDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<LeaveRequestDto>> list(@PathVariable Long companyId) {
        return ResponseEntity.ok(leaveRequestService.listByCompany(companyId));
    }

    @Operation(summary = "Update leave status", description = "Approve/reject or change leave status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave updated",
                    content = @Content(schema = @Schema(implementation = LeaveRequestDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{leaveId}/status")
    public ResponseEntity<LeaveRequestDto> updateStatus(@PathVariable Long companyId,
                                                        @PathVariable Long leaveId,
                                                        @RequestBody LeaveStatusUpdateDto request) {
        return ResponseEntity.ok(leaveRequestService.updateStatus(companyId, leaveId, request));
    }

    @Operation(summary = "Export leaves CSV", description = "Export all leaves for the company as CSV")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV streamed")
    })
    @GetMapping("/export")
    public void exportCsv(@PathVariable Long companyId,
                          HttpServletResponse response) throws Exception {

        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=leaves.csv");

        List<LeaveRequestDto> list = leaveRequestService.findByCompany(companyId);

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Employee Code,Employee Name,Start Date,End Date,Type,Status,Decision By,Decision Comment");

            for (LeaveRequestDto l : list) {
                String line = String.join(",",
                        safe(l.getEmployeeCode()),
                        safe(l.getEmployeeName()),
                        safe(l.getStartDate() != null ? l.getStartDate().toString() : ""),
                        safe(l.getEndDate() != null ? l.getEndDate().toString() : ""),
                        safe(l.getLeaveType()),
                        safe(l.getStatus()),
                        safe(l.getDecidedByName()),
                        safe(l.getDecisionComment())
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
