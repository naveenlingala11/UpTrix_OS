package com.uptrix.uptrix_backend.mapper;

import com.uptrix.uptrix_backend.dto.platform.EnvironmentPresetDto;
import com.uptrix.uptrix_backend.dto.platform.PlanPresetDto;
import com.uptrix.uptrix_backend.dto.platform.PlatformSettingsDto;
import com.uptrix.uptrix_backend.entity.platform.EnvironmentPreset;
import com.uptrix.uptrix_backend.entity.platform.PlanPreset;
import com.uptrix.uptrix_backend.entity.platform.PlatformSettings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps between PlatformSettings JPA entities and DTOs.
 */
@Component
public class PlatformSettingsMapper {

    // ========= PUBLIC API =========

    public PlatformSettingsDto toDto(PlatformSettings entity) {
        if (entity == null) {
            return null;
        }

        PlatformSettingsDto dto = new PlatformSettingsDto();

        // ---------- Tenant defaults ----------
        dto.setDefaultTenantStatus(entity.getDefaultTenantStatus());
        dto.setDefaultTrialDays(entity.getDefaultTrialDays());
        dto.setDefaultTimeZone(entity.getDefaultTimeZone());
        dto.setDefaultWeekStart(entity.getDefaultWeekStart());
        dto.setDefaultCurrency(entity.getDefaultCurrency());
        dto.setDefaultLocale(entity.getDefaultLocale());

        // ---------- Security & access ----------
        dto.setEnforceStrongPasswords(entity.isEnforceStrongPasswords());
        dto.setSsoEnforcedForAdmins(entity.isSsoEnforcedForAdmins());
        dto.setIpRestrictionEnabled(entity.isIpRestrictionEnabled());
        dto.setIpRestrictionNote(entity.getIpRestrictionNote());

        // ---------- Notifications ----------
        dto.setNotifyCriticalToSuperAdmins(entity.isNotifyCriticalToSuperAdmins());
        dto.setNotifyUsageToTenantAdmins(entity.isNotifyUsageToTenantAdmins());
        dto.setWeeklyDigestEnabled(entity.isWeeklyDigestEnabled());

        // ---------- Audit / retention ----------
        dto.setAuditLogRetentionDays(entity.getAuditLogRetentionDays());
        dto.setActivityLogRetentionDays(entity.getActivityLogRetentionDays());
        dto.setEnableTenantLevelExports(entity.isEnableTenantLevelExports());

        // ---------- Global service & support ----------
        dto.setGlobalSupportEmail(entity.getGlobalSupportEmail());
        dto.setGlobalSupportPhone(entity.getGlobalSupportPhone());
        dto.setSupportPortalUrl(entity.getSupportPortalUrl());
        dto.setDefaultSlaTier(entity.getDefaultSlaTier());
        dto.setSupportHoursLabel(entity.getSupportHoursLabel());
        dto.setShowInAppHelpWidget(entity.isShowInAppHelpWidget());
        dto.setEnableStatusPageIntegration(entity.isEnableStatusPageIntegration());
        dto.setStatusPageUrl(entity.getStatusPageUrl());

        // ---------- Maintenance & incidents ----------
        dto.setMaintenanceWindowDay(entity.getMaintenanceWindowDay());
        dto.setMaintenanceWindowTime(entity.getMaintenanceWindowTime());
        dto.setAutoNotifyMaintenanceToAdmins(entity.isAutoNotifyMaintenanceToAdmins());
        dto.setAutoNotifyIncidentsToAdmins(entity.isAutoNotifyIncidentsToAdmins());
        dto.setIncidentCommChannel(entity.getIncidentCommChannel());

        // ---------- Child collections ----------
        if (entity.getPlanPresets() != null) {
            dto.setPlanPresets(
                    entity.getPlanPresets()
                            .stream()
                            .map(this::toDto)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setPlanPresets(new ArrayList<>());
        }

        if (entity.getEnvironments() != null) {
            dto.setEnvironments(
                    entity.getEnvironments()
                            .stream()
                            .map(this::toDto)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setEnvironments(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Apply values from DTO into an existing PlatformSettings entity.
     * Used for PUT /api/platform/settings.
     */
    public void updateEntityFromDto(PlatformSettingsDto dto, PlatformSettings entity) {
        if (dto == null || entity == null) {
            return;
        }

        // ---------- Tenant defaults ----------
        entity.setDefaultTenantStatus(dto.getDefaultTenantStatus());
        entity.setDefaultTrialDays(dto.getDefaultTrialDays());
        entity.setDefaultTimeZone(dto.getDefaultTimeZone());
        entity.setDefaultWeekStart(dto.getDefaultWeekStart());
        entity.setDefaultCurrency(dto.getDefaultCurrency());
        entity.setDefaultLocale(dto.getDefaultLocale());

        // ---------- Security & access ----------
        entity.setEnforceStrongPasswords(dto.isEnforceStrongPasswords());
        entity.setSsoEnforcedForAdmins(dto.isSsoEnforcedForAdmins());
        entity.setIpRestrictionEnabled(dto.isIpRestrictionEnabled());
        entity.setIpRestrictionNote(dto.getIpRestrictionNote());

        // ---------- Notifications ----------
        entity.setNotifyCriticalToSuperAdmins(dto.isNotifyCriticalToSuperAdmins());
        entity.setNotifyUsageToTenantAdmins(dto.isNotifyUsageToTenantAdmins());
        entity.setWeeklyDigestEnabled(dto.isWeeklyDigestEnabled());

        // ---------- Audit / retention ----------
        entity.setAuditLogRetentionDays(dto.getAuditLogRetentionDays());
        entity.setActivityLogRetentionDays(dto.getActivityLogRetentionDays());
        entity.setEnableTenantLevelExports(dto.isEnableTenantLevelExports());

        // ---------- Global service & support ----------
        entity.setGlobalSupportEmail(dto.getGlobalSupportEmail());
        entity.setGlobalSupportPhone(dto.getGlobalSupportPhone());
        entity.setSupportPortalUrl(dto.getSupportPortalUrl());
        entity.setDefaultSlaTier(dto.getDefaultSlaTier());
        entity.setSupportHoursLabel(dto.getSupportHoursLabel());
        entity.setShowInAppHelpWidget(dto.isShowInAppHelpWidget());
        entity.setEnableStatusPageIntegration(dto.isEnableStatusPageIntegration());
        entity.setStatusPageUrl(dto.getStatusPageUrl());

        // ---------- Maintenance & incidents ----------
        entity.setMaintenanceWindowDay(dto.getMaintenanceWindowDay());
        entity.setMaintenanceWindowTime(dto.getMaintenanceWindowTime());
        entity.setAutoNotifyMaintenanceToAdmins(dto.isAutoNotifyMaintenanceToAdmins());
        entity.setAutoNotifyIncidentsToAdmins(dto.isAutoNotifyIncidentsToAdmins());
        entity.setIncidentCommChannel(dto.getIncidentCommChannel());

        // ---------- Child collections ----------
        syncPlanPresets(dto.getPlanPresets(), entity);
        syncEnvironments(dto.getEnvironments(), entity);
    }

    // ========= PRIVATE HELPERS =========

    private PlanPresetDto toDto(PlanPreset p) {
        if (p == null) {
            return null;
        }
        PlanPresetDto dto = new PlanPresetDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setMaxEmployees(p.getMaxEmployees());
        dto.setModules(p.getModules() != null
                ? new ArrayList<>(p.getModules())
                : new ArrayList<>());
        dto.setRecommended(p.isRecommended());
        return dto;
    }

    private EnvironmentPresetDto toDto(EnvironmentPreset e) {
        if (e == null) {
            return null;
        }
        EnvironmentPresetDto dto = new EnvironmentPresetDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setKey(e.getEnvKey()); // ✅ from envKey
        dto.setStatus(e.getStatus());
        dto.setRegion(e.getRegion());
        return dto;
    }

    private void syncPlanPresets(List<PlanPresetDto> presetDtos, PlatformSettings entity) {
        entity.getPlanPresets().clear();

        if (presetDtos == null || presetDtos.isEmpty()) {
            return;
        }

        for (PlanPresetDto dto : presetDtos) {
            PlanPreset p = new PlanPreset();
            p.setPlatformSettings(entity);
            p.setName(dto.getName());
            p.setDescription(dto.getDescription());
            p.setMaxEmployees(dto.getMaxEmployees());
            p.setModules(dto.getModules() != null
                    ? new ArrayList<>(dto.getModules())
                    : new ArrayList<>());
            p.setRecommended(dto.isRecommended());
            entity.getPlanPresets().add(p);
        }
    }

    private void syncEnvironments(List<EnvironmentPresetDto> envDtos, PlatformSettings entity) {
        entity.getEnvironments().clear();

        if (envDtos == null || envDtos.isEmpty()) {
            return;
        }

        for (EnvironmentPresetDto dto : envDtos) {
            EnvironmentPreset env = new EnvironmentPreset();
            env.setPlatformSettings(entity);
            env.setName(dto.getName());
            env.setEnvKey(dto.getKey()); // ✅ to envKey
            env.setStatus(dto.getStatus());
            env.setRegion(dto.getRegion());
            entity.getEnvironments().add(env);
        }
    }
}
