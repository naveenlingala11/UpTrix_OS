package com.uptrix.uptrix_backend.dto.platform;

import com.uptrix.uptrix_backend.entity.platform.enums.IncidentCommChannel;
import com.uptrix.uptrix_backend.entity.platform.enums.SlaTier;
import com.uptrix.uptrix_backend.entity.platform.enums.TenantStatus;
import com.uptrix.uptrix_backend.entity.platform.enums.WeekStart;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlatformSettingsDto {

    // Tenant defaults
    private TenantStatus defaultTenantStatus;
    private int defaultTrialDays;
    private String defaultTimeZone;
    private WeekStart defaultWeekStart;
    private String defaultCurrency;
    private String defaultLocale;

    // Security & access
    private boolean enforceStrongPasswords;
    private boolean ssoEnforcedForAdmins;
    private boolean ipRestrictionEnabled;
    private String ipRestrictionNote;

    // Notifications
    private boolean notifyCriticalToSuperAdmins;
    private boolean notifyUsageToTenantAdmins;
    private boolean weeklyDigestEnabled;

    // Audit / retention
    private int auditLogRetentionDays;
    private int activityLogRetentionDays;
    private boolean enableTenantLevelExports;

    // Global service & support posture
    private String globalSupportEmail;
    private String globalSupportPhone;
    private String supportPortalUrl;
    private SlaTier defaultSlaTier;
    private String supportHoursLabel;
    private boolean showInAppHelpWidget;
    private boolean enableStatusPageIntegration;
    private String statusPageUrl;

    // Maintenance & incident communication
    private String maintenanceWindowDay;
    private String maintenanceWindowTime;
    private boolean autoNotifyMaintenanceToAdmins;
    private boolean autoNotifyIncidentsToAdmins;
    private IncidentCommChannel incidentCommChannel;

    // Collections
    private List<PlanPresetDto> planPresets;
    private List<EnvironmentPresetDto> environments;
}
