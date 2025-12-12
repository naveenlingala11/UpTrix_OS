package com.uptrix.uptrix_backend.controller.platform;

import com.uptrix.uptrix_backend.dto.UserPrincipal;
import com.uptrix.uptrix_backend.dto.platform.PlatformSettingsDto;
import com.uptrix.uptrix_backend.security.SecurityUtils;
import com.uptrix.uptrix_backend.service.platform.PlatformSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform")
public class PlatformSettingsController {

    private final PlatformSettingsService platformSettingsService;

    public PlatformSettingsController(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    private void ensureSuperAdmin() {
        UserPrincipal user = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("No authenticated user"));

        if (!user.hasRole("SUPER_ADMIN")) {
            throw new AccessDeniedException("Only super admins can manage platform settings");
        }
    }

    @Operation(summary = "Get platform settings", description = "Get global platform settings (super admin only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/settings")
    public ResponseEntity<PlatformSettingsDto> getSettings() {
        ensureSuperAdmin();
        PlatformSettingsDto dto = platformSettingsService.getSettings();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update platform settings", description = "Update platform settings (super admin only)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/settings")
    public ResponseEntity<PlatformSettingsDto> updateSettings(@RequestBody PlatformSettingsDto dto) {
        ensureSuperAdmin();
        PlatformSettingsDto updated = platformSettingsService.updateSettings(dto);
        return ResponseEntity.ok(updated);
    }
}
