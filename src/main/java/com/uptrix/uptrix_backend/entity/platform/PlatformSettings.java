package com.uptrix.uptrix_backend.entity.platform;

import com.uptrix.uptrix_backend.entity.platform.enums.IncidentCommChannel;
import com.uptrix.uptrix_backend.entity.platform.enums.SlaTier;
import com.uptrix.uptrix_backend.entity.platform.enums.TenantStatus;
import com.uptrix.uptrix_backend.entity.platform.enums.WeekStart;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
public class PlatformSettings {

    @Id
    private Long id; // singleton row (we use id = 1)

    // ---------- Tenant defaults ----------
    @Enumerated(EnumType.STRING)
    @Column(name = "default_tenant_status", nullable = false, length = 20)
    private TenantStatus defaultTenantStatus;

    @Column(name = "default_trial_days", nullable = false)
    private int defaultTrialDays;

    @Column(name = "default_time_zone", nullable = false, length = 100)
    private String defaultTimeZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_week_start", nullable = false, length = 20)
    private WeekStart defaultWeekStart;

    @Column(name = "default_currency", nullable = false, length = 10)
    private String defaultCurrency;

    @Column(name = "default_locale", nullable = false, length = 20)
    private String defaultLocale;

    // ---------- Security & access ----------
    @Column(name = "enforce_strong_passwords", nullable = false)
    private boolean enforceStrongPasswords;

    @Column(name = "sso_enforced_for_admins", nullable = false)
    private boolean ssoEnforcedForAdmins;

    @Column(name = "ip_restriction_enabled", nullable = false)
    private boolean ipRestrictionEnabled;

    @Column(name = "ip_restriction_note", length = 255)
    private String ipRestrictionNote;

    // ---------- Notifications ----------
    @Column(name = "notify_critical_to_super_admins", nullable = false)
    private boolean notifyCriticalToSuperAdmins;

    @Column(name = "notify_usage_to_tenant_admins", nullable = false)
    private boolean notifyUsageToTenantAdmins;

    @Column(name = "weekly_digest_enabled", nullable = false)
    private boolean weeklyDigestEnabled;

    // ---------- Audit / retention ----------
    @Column(name = "audit_log_retention_days", nullable = false)
    private int auditLogRetentionDays;

    @Column(name = "activity_log_retention_days", nullable = false)
    private int activityLogRetentionDays;

    @Column(name = "enable_tenant_level_exports", nullable = false)
    private boolean enableTenantLevelExports;

    // ---------- Global service & support ----------
    @Column(name = "global_support_email", length = 150)
    private String globalSupportEmail;

    @Column(name = "global_support_phone", length = 80)
    private String globalSupportPhone;

    @Column(name = "support_portal_url", length = 255)
    private String supportPortalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_sla_tier", nullable = false, length = 20)
    private SlaTier defaultSlaTier;

    @Column(name = "support_hours_label", length = 120)
    private String supportHoursLabel;

    @Column(name = "show_in_app_help_widget", nullable = false)
    private boolean showInAppHelpWidget;

    @Column(name = "enable_status_page_integration", nullable = false)
    private boolean enableStatusPageIntegration;

    @Column(name = "status_page_url", length = 255)
    private String statusPageUrl;

    // ---------- Maintenance & incidents ----------
    @Column(name = "maintenance_window_day", length = 40)
    private String maintenanceWindowDay;

    @Column(name = "maintenance_window_time", length = 100)
    private String maintenanceWindowTime;

    @Column(name = "auto_notify_maintenance_admins", nullable = false)
    private boolean autoNotifyMaintenanceToAdmins;

    @Column(name = "auto_notify_incidents_admins", nullable = false)
    private boolean autoNotifyIncidentsToAdmins;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_comm_channel", nullable = false, length = 30)
    private IncidentCommChannel incidentCommChannel;

    // ---------- Child collections ----------
    @OneToMany(
            mappedBy = "platformSettings",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlanPreset> planPresets = new ArrayList<>();

    @OneToMany(
            mappedBy = "platformSettings",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<EnvironmentPreset> environments = new ArrayList<>();
}
