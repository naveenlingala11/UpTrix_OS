package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.company.DepartmentAttendanceSummaryDto;
import com.uptrix.uptrix_backend.dto.company.RecentLeaveDto;
import com.uptrix.uptrix_backend.service.dashboard.HrDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/hr-dashboard")
@Tag(name = "HR Dashboard", description = "HR dashboard widgets for tenant")
public class HrDashboardController {

    private final HrDashboardService hrDashboardService;

    public HrDashboardController(HrDashboardService hrDashboardService) {
        this.hrDashboardService = hrDashboardService;
    }

    @Operation(summary = "Department attendance today",
            description = "Get attendance summary per department for today")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DepartmentAttendanceSummaryDto.class))))
    })
    @GetMapping("/attendance-departments-today")
    public ResponseEntity<List<DepartmentAttendanceSummaryDto>> getTodayDeptAttendance(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(hrDashboardService.getTodayDepartmentAttendance(companyId));
    }

    @Operation(summary = "Recent leaves", description = "Get recent leave requests for the company")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recent leaves returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecentLeaveDto.class))))
    })
    @GetMapping("/recent-leaves")
    public ResponseEntity<List<RecentLeaveDto>> getRecentLeaves(
            @PathVariable Long companyId,
            @RequestParam(name = "limit", defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(hrDashboardService.getRecentLeaves(companyId, limit));
    }
}
