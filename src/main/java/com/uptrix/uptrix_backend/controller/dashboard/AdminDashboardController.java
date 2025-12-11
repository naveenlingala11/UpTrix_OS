package com.uptrix.uptrix_backend.controller.dashboard;

import com.uptrix.uptrix_backend.dto.company.CompanyStatsDto;
import com.uptrix.uptrix_backend.dto.dashboard.AdminOrgHealthDto;
import com.uptrix.uptrix_backend.dto.dashboard.OrgSummaryResponse;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.DepartmentRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import com.uptrix.uptrix_backend.service.company.CompanyStatsService;
import com.uptrix.uptrix_backend.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final CompanyStatsService companyStatsService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public AdminDashboardController(
            CompanyStatsService companyStatsService,
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {
        this.companyStatsService = companyStatsService;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Operation(summary = "Organization summary", description = "Summary for admin dashboard",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/org-summary")
    public ResponseEntity<OrgSummaryResponse> getOrgSummary() {

        // ✅ Get logged-in user
        Long userId = SecurityUtils.getCurrentUser()
                .map(p -> p.getUserId())
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long companyId = user.getCompany().getId();

        // ✅ Fetch real company stats
        CompanyStatsDto stats = companyStatsService.getStats(companyId);

        // ✅ Map to widget response
        OrgSummaryResponse resp = new OrgSummaryResponse();

        resp.setCompanies(1); // tenant admin = 1 company
        resp.setActiveUsers((int) stats.getTotalUsers());

        int licensedSeats = 40; // TODO: move to DB later
        int utilisation = (int) Math.round((stats.getTotalUsers() * 100.0) / licensedSeats);
        utilisation = Math.max(0, Math.min(100, utilisation));

        resp.setLicenseUtilization(utilisation);

        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Org health snapshot", description = "Organization health metrics for tenant admin",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/org-health")
    public ResponseEntity<AdminOrgHealthDto> getOrgHealth(
            @RequestParam("companyId") Long companyId
    ) {

        long totalEmployees = userRepository.countByCompanyId(companyId);
        long totalDepartments = departmentRepository.countByCompanyId(companyId);

        double avgEmployeesPerDepartment = 0.0;
        if (totalDepartments > 0) {
            avgEmployeesPerDepartment = (double) totalEmployees / (double) totalDepartments;
        }

        AdminOrgHealthDto dto = new AdminOrgHealthDto();
        dto.setCompanyId(companyId);
        dto.setTotalEmployees(totalEmployees);
        dto.setTotalDepartments(totalDepartments);
        dto.setAvgEmployeesPerDepartment(avgEmployeesPerDepartment);

        return ResponseEntity.ok(dto);
    }
}
