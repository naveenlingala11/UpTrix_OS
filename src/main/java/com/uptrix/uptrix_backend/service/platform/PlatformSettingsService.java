package com.uptrix.uptrix_backend.service.platform;

import com.uptrix.uptrix_backend.dto.platform.PlatformSettingsDto;
import com.uptrix.uptrix_backend.entity.platform.EnvironmentPreset;
import com.uptrix.uptrix_backend.entity.platform.PlanPreset;
import com.uptrix.uptrix_backend.entity.platform.PlatformSettings;
import com.uptrix.uptrix_backend.entity.platform.enums.EnvironmentStatus;
import com.uptrix.uptrix_backend.entity.platform.enums.IncidentCommChannel;
import com.uptrix.uptrix_backend.entity.platform.enums.SlaTier;
import com.uptrix.uptrix_backend.entity.platform.enums.TenantStatus;
import com.uptrix.uptrix_backend.entity.platform.enums.WeekStart;
import com.uptrix.uptrix_backend.mapper.PlatformSettingsMapper;
import com.uptrix.uptrix_backend.repository.platform.PlatformSettingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformSettingsService {

    private final PlatformSettingsRepository repo;
    private final PlatformSettingsMapper mapper;

    /**
     * Return the singleton platform settings row.
     * If none exists, create with sensible defaults.
     */
    public PlatformSettingsDto getSettings() {
        PlatformSettings entity = repo.findById(1L).orElseGet(this::createDefaultSettings);
        return mapper.toDto(entity);
    }
    /**
     * Update the singleton platform settings row.
     */
    public PlatformSettingsDto updateSettings(PlatformSettingsDto dto) {
        PlatformSettings entity = repo.findAll().stream().findFirst()
                .orElseGet(this::createDefaultSettings);

        mapper.updateEntityFromDto(dto, entity);

        System.out.println(">>> Updating platform settings: "
                + "defaultCurrency=" + entity.getDefaultCurrency()
                + ", defaultTimeZone=" + entity.getDefaultTimeZone()
                + ", weeklyDigestEnabled=" + entity.isWeeklyDigestEnabled());

        PlatformSettings saved = repo.save(entity);

        System.out.println(">>> Saved platform settings with id=" + saved.getId());

        return mapper.toDto(saved);
    }

    /**
     * Create and persist a default PlatformSettings row (id = 1).
     * This is called lazily if no settings exist yet.
     */
    private PlatformSettings createDefaultSettings() {
        PlatformSettings s = new PlatformSettings();

        // Singleton row
        s.setId(1L);

        // ---------- Tenant defaults ----------
        s.setDefaultTenantStatus(TenantStatus.TRIAL);
        s.setDefaultTrialDays(21);
        s.setDefaultTimeZone("Asia/Kolkata");
        s.setDefaultWeekStart(WeekStart.MONDAY);
        s.setDefaultCurrency("INR");
        s.setDefaultLocale("en-IN");

        // ---------- Security & access ----------
        s.setEnforceStrongPasswords(true);
        s.setSsoEnforcedForAdmins(false);
        s.setIpRestrictionEnabled(false);
        s.setIpRestrictionNote("E.g. 10.10.0.0/16, 192.168.1.0/24");

        // ---------- Notifications ----------
        s.setNotifyCriticalToSuperAdmins(true);
        s.setNotifyUsageToTenantAdmins(true);
        s.setWeeklyDigestEnabled(true);

        // ---------- Audit / retention ----------
        s.setAuditLogRetentionDays(365);
        s.setActivityLogRetentionDays(180);
        s.setEnableTenantLevelExports(true);

        // ---------- Global service & support ----------
        s.setGlobalSupportEmail("support@uptrix.com");
        s.setGlobalSupportPhone("+1-800-UPTRIX");
        s.setSupportPortalUrl("https://support.uptrix.com");
        s.setDefaultSlaTier(SlaTier.STANDARD);
        s.setSupportHoursLabel("24x5 (Mon–Fri)");
        s.setShowInAppHelpWidget(true);
        s.setEnableStatusPageIntegration(true);
        s.setStatusPageUrl("https://status.uptrix.com");

        // ---------- Maintenance & incidents ----------
        s.setMaintenanceWindowDay("Saturday");
        s.setMaintenanceWindowTime("01:00 – 03:00 IST");
        s.setAutoNotifyMaintenanceToAdmins(true);
        s.setAutoNotifyIncidentsToAdmins(true);
        s.setIncidentCommChannel(IncidentCommChannel.EMAIL_AND_IN_APP);

        s.setPlanPresets(new ArrayList<>());
        s.setEnvironments(new ArrayList<>());

        // ---------- Plan presets ----------
        PlanPreset starter = new PlanPreset();
        starter.setPlatformSettings(s);
        starter.setName("Starter HR");
        starter.setDescription("Core HR + attendance for small teams.");
        starter.setMaxEmployees(200);
        starter.setModules(new ArrayList<>());
        starter.getModules().add("Core HR");
        starter.getModules().add("Attendance");
        starter.getModules().add("Leaves");
        starter.setRecommended(false);
        s.getPlanPresets().add(starter);

        PlanPreset growth = new PlanPreset();
        growth.setPlatformSettings(s);
        growth.setName("Growth Suite");
        growth.setDescription("HR + Projects + basic CRM for growing companies.");
        growth.setMaxEmployees(1000);
        growth.setModules(new ArrayList<>());
        growth.getModules().add("Core HR");
        growth.getModules().add("Attendance");
        growth.getModules().add("Leaves");
        growth.getModules().add("Projects");
        growth.getModules().add("Basic CRM");
        growth.setRecommended(true);
        s.getPlanPresets().add(growth);

        PlanPreset enterprise = new PlanPreset();
        enterprise.setPlatformSettings(s);
        enterprise.setName("Enterprise");
        enterprise.setDescription("Full Uptrix stack with custom SLAs.");
        enterprise.setMaxEmployees(10000);
        enterprise.setModules(new ArrayList<>());
        enterprise.getModules().add("Core HR");
        enterprise.getModules().add("Attendance");
        enterprise.getModules().add("Leaves");
        enterprise.getModules().add("Projects");
        enterprise.getModules().add("CRM");
        enterprise.getModules().add("Reports");
        enterprise.setRecommended(false);
        s.getPlanPresets().add(enterprise);

        // ---------- Environments ----------
        EnvironmentPreset prod = new EnvironmentPreset();
        prod.setPlatformSettings(s);
        prod.setName("Production");
        prod.setEnvKey("prod");
        prod.setStatus(EnvironmentStatus.LIVE);
        prod.setRegion("ap-south-1");
        s.getEnvironments().add(prod);

        EnvironmentPreset sandbox = new EnvironmentPreset();
        sandbox.setPlatformSettings(s);
        sandbox.setName("Sandbox");
        sandbox.setEnvKey("sandbox");
        sandbox.setStatus(EnvironmentStatus.SANDBOX);
        sandbox.setRegion("ap-south-1");
        s.getEnvironments().add(sandbox);

        EnvironmentPreset staging = new EnvironmentPreset();
        staging.setPlatformSettings(s);
        staging.setName("Staging");
        staging.setEnvKey("staging");
        staging.setStatus(EnvironmentStatus.MAINTENANCE);
        staging.setRegion("ap-south-1");
        s.getEnvironments().add(staging);

        return repo.save(s);
    }
}
