package com.uptrix.uptrix_backend.controller.dashboard;

import com.uptrix.uptrix_backend.dto.audit.AuditLogDto;
import com.uptrix.uptrix_backend.dto.dashboard.*;
import com.uptrix.uptrix_backend.entity.company.Company;
import com.uptrix.uptrix_backend.repository.CompanyRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import com.uptrix.uptrix_backend.service.AuditLogService;
import com.uptrix.uptrix_backend.service.TenantFeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminDashboardController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final TenantFeatureService tenantFeatureService;

    public SuperAdminDashboardController(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            AuditLogService auditLogService,
            TenantFeatureService tenantFeatureService
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.tenantFeatureService = tenantFeatureService;
    }

    @Operation(summary = "Super-admin summary", description = "Summary cards for super admin dashboard",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/summary")
    public ResponseEntity<SuperAdminSummaryDto> getSummary() {

        SuperAdminSummaryDto dto = new SuperAdminSummaryDto();

        long totalCompanies = companyRepository.count();
        long activeTenants = companyRepository.countByStatus("ACTIVE");
        long totalUsers = userRepository.count();

        dto.setTotalCompanies(totalCompanies);
        dto.setActiveTenants(activeTenants);
        dto.setTotalUsers(totalUsers);

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Top tenants", description = "Return top tenants ordered by recent activity",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tenants/top")
    public ResponseEntity<List<TenantMiniViewDto>> getTopTenants() {
        List<Company> companies = companyRepository.findTop10ByOrderByUpdatedAtDesc();
        if (companies.isEmpty()) {
            companies = companyRepository.findTop10ByOrderByIdDesc();
        }

        List<TenantMiniViewDto> result = new ArrayList<>();
        for (Company c : companies) {
            result.add(toTenantMiniView(c));
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Platform alerts", description = "List platform alerts for super admin",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/alerts")
    public ResponseEntity<List<PlatformAlertDto>> getAlerts() {
        List<PlatformAlertDto> alerts = new ArrayList<>();

        // WARN: All TRIAL tenants
        List<Company> trialCompanies = companyRepository.findByStatus("TRIAL");
        for (Company c : trialCompanies) {
            PlatformAlertDto alert = new PlatformAlertDto();
            alert.setType("WARN");
            alert.setTitle("Trial tenant active");
            alert.setDescription("Trial tenant \"" + c.getName() + "\" is currently active.");
            alert.setScope(c.getName());
            alerts.add(alert);
        }

        // INFO: overall platform usage
        long totalCompanies = companyRepository.count();
        long totalUsers = userRepository.count();
        if (totalCompanies > 0) {
            PlatformAlertDto usage = new PlatformAlertDto();
            usage.setType("INFO");
            usage.setTitle("Platform usage");
            usage.setDescription("You have " + totalCompanies + " tenants and " + totalUsers + " users on Uptrix.");
            usage.setScope("Platform");
            alerts.add(usage);
        }

        return ResponseEntity.ok(alerts);
    }

    @Operation(summary = "Pause tenant", description = "Pause a tenant (super admin action)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tenants/{id}/pause")
    public ResponseEntity<TenantMiniViewDto> pauseTenant(@PathVariable Long id) {
        Optional<Company> opt = companyRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Company c = opt.get();
        c.setStatus("PAUSED");
        Company saved = companyRepository.save(c);

        auditLogService.logSuperAdminAction(
                "TENANT_PAUSED",
                "COMPANY",
                saved.getId(),
                saved.getName(),
                "Tenant paused by super admin"
        );

        return ResponseEntity.ok(toTenantMiniView(saved));
    }

    @Operation(summary = "Resume tenant", description = "Resume a paused tenant",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tenants/{id}/resume")
    public ResponseEntity<TenantMiniViewDto> resumeTenant(@PathVariable Long id) {
        Optional<Company> opt = companyRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Company c = opt.get();
        c.setStatus("ACTIVE");
        Company saved = companyRepository.save(c);

        auditLogService.logSuperAdminAction(
                "TENANT_RESUMED",
                "COMPANY",
                saved.getId(),
                saved.getName(),
                "Tenant resumed by super admin"
        );

        return ResponseEntity.ok(toTenantMiniView(saved));
    }

    @Operation(summary = "Extend trial", description = "Extend trial for tenant",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tenants/{id}/extend-trial")
    public ResponseEntity<TenantMiniViewDto> extendTrial(
            @PathVariable Long id,
            @RequestParam(name = "days", defaultValue = "7") int days
    ) {
        Optional<Company> opt = companyRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Company c = opt.get();

        LocalDateTime base = c.getTrialEndsAt();
        if (base == null) {
            base = LocalDateTime.now();
        }
        c.setTrialEndsAt(base.plusDays(days));

        if (c.getStatus() == null || c.getStatus().isBlank()) {
            c.setStatus("TRIAL");
        }

        Company saved = companyRepository.save(c);

        auditLogService.logSuperAdminAction(
                "TENANT_TRIAL_EXTENDED",
                "COMPANY",
                saved.getId(),
                saved.getName(),
                "Trial extended by " + days + " days"
        );

        return ResponseEntity.ok(toTenantMiniView(saved));
    }

    @Operation(summary = "Change tenant plan", description = "Change subscription plan for tenant",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/tenants/{id}/change-plan")
    public ResponseEntity<TenantMiniViewDto> changePlan(
            @PathVariable Long id,
            @RequestBody ChangePlanRequestDto request
    ) {
        Optional<Company> opt = companyRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Company c = opt.get();
        c.setPlanCode(request.getPlanCode());
        Company saved = companyRepository.save(c);

        auditLogService.logSuperAdminAction(
                "TENANT_PLAN_CHANGED",
                "COMPANY",
                saved.getId(),
                saved.getName(),
                "Plan changed to " + request.getPlanCode()
        );

        return ResponseEntity.ok(toTenantMiniView(saved));
    }

    @Operation(summary = "Recent audit logs", description = "Return recent audit logs for dashboard widget",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/audit/recent")
    public ResponseEntity<List<AuditLogDto>> getRecentAuditLogs(
            @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        if (limit <= 0 || limit > 50) {
            limit = 20;
        }
        List<AuditLogDto> logs = auditLogService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    @Operation(summary = "Tenant features", description = "Get feature flags for a tenant",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tenants/{id}/features")
    public ResponseEntity<TenantFeaturesDto> getTenantFeatures(@PathVariable Long id) {
        TenantFeaturesDto dto = tenantFeatureService.getFeaturesForTenant(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update tenant features", description = "Update feature flags for a tenant",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/tenants/{id}/features")
    public ResponseEntity<TenantFeaturesDto> updateTenantFeatures(@PathVariable Long id,
                                                                  @RequestBody TenantFeaturesDto request) {
        TenantFeaturesDto updated = tenantFeatureService.updateFeaturesForTenant(id, request);

        auditLogService.logSuperAdminAction(
                "TENANT_FEATURES_UPDATED",
                "COMPANY",
                updated.getCompanyId(),
                null,
                "Updated feature flags for tenant " + updated.getCompanyId()
        );

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "License usage", description = "Return license / seat utilisation across tenants",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/license-usage")
    public ResponseEntity<LicenseUsageDto> getLicenseUsage() {

        List<Company> companies = companyRepository.findAll();
        List<TenantLicenseUsageDto> tenantList = new ArrayList<>();

        long totalSeatLimit = 0;
        long totalUsedSeats = 0;

        for (Company c : companies) {
            TenantLicenseUsageDto t = new TenantLicenseUsageDto();
            t.setCompanyId(c.getId());
            t.setCompanyName(c.getName());

            Integer seatLimit = c.getSeatLimit();
            if (seatLimit == null) {
                seatLimit = 0;
            }
            t.setSeatLimit(seatLimit);

            long usedSeats = userRepository.countByCompanyId(c.getId());
            t.setUsedSeats(usedSeats);

            int utilisation = 0;
            if (seatLimit != null && seatLimit > 0) {
                utilisation = (int) ((usedSeats * 100) / seatLimit);
            }
            t.setUtilisationPercent(utilisation);

            totalSeatLimit += seatLimit;
            totalUsedSeats += usedSeats;

            tenantList.add(t);
        }

        // Sort tenants by utilisation descending
        tenantList = tenantList.stream()
                .sorted(Comparator.comparingInt(TenantLicenseUsageDto::getUtilisationPercent).reversed())
                .collect(Collectors.toList());

        LicenseUsageDto dto = new LicenseUsageDto();
        dto.setTotalSeatLimit(totalSeatLimit);
        dto.setTotalUsedSeats(totalUsedSeats);
        dto.setTenants(tenantList);

        return ResponseEntity.ok(dto);
    }

    // ---------- helpers ----------
    private TenantMiniViewDto toTenantMiniView(Company c) {
        TenantMiniViewDto dto = new TenantMiniViewDto();
        dto.setId(c.getId());
        dto.setCompanyName(c.getName());
        dto.setSubdomain(c.getSubdomain());

        String status = c.getStatus();
        if (status == null || status.isBlank()) {
            dto.setStatus("UNKNOWN");
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            dto.setStatus("LIVE");
        } else {
            dto.setStatus(status.toUpperCase());
        }

        long userCount = userRepository.countByCompanyId(c.getId());
        dto.setUsers(userCount);

        // TODO: refine admin counting later (based on roles)
        dto.setAdmins(0L);

        LocalDateTime lastChange = c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt();
        dto.setLastActive(formatLastActiveLabel(lastChange));

        return dto;
    }

    private String formatLastActiveLabel(LocalDateTime ts) {
        if (ts == null) {
            return "N/A";
        }
        LocalDateTime now = LocalDateTime.now();
        Duration d = Duration.between(ts, now);
        long minutes = d.toMinutes();

        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = d.toDays();
        if (days < 7) {
            return days + "d ago";
        }
        return ts.toLocalDate().toString(); // YYYY-MM-DD
    }
}
