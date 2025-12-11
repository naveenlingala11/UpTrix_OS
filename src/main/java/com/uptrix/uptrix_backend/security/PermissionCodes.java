package com.uptrix.uptrix_backend.security;

public final class PermissionCodes {

    private PermissionCodes() {}

    // Company / config
    public static final String COMPANY_CONFIG = "COMPANY_CONFIG";

    // Master data
    public static final String EMPLOYEE_VIEW   = "EMPLOYEE_VIEW";
    public static final String EMPLOYEE_MANAGE = "EMPLOYEE_MANAGE";
    public static final String DEPARTMENT_MANAGE = "DEPARTMENT_MANAGE";
    public static final String SHIFT_MANAGE = "SHIFT_MANAGE";

    // Attendance & leaves
    public static final String ATTENDANCE_VIEW_ALL = "ATTENDANCE_VIEW_ALL";
    public static final String ATTENDANCE_TEAM_VIEW = "ATTENDANCE_TEAM_VIEW";

    public static final String LEAVE_VIEW_ALL = "LEAVE_VIEW_ALL";
    public static final String LEAVE_APPROVE = "LEAVE_APPROVE";
    public static final String LEAVE_TEAM_APPROVE = "LEAVE_TEAM_APPROVE";

    // Helpdesk
    public static final String HELPDESK_VIEW_ALL = "HELPDESK_VIEW_ALL";
    public static final String HELPDESK_MANAGE   = "HELPDESK_MANAGE";

    // Employee self-service (for future)
    public static final String SELF_PROFILE_VIEW = "SELF_PROFILE_VIEW";
    public static final String SELF_ATTENDANCE_VIEW = "SELF_ATTENDANCE_VIEW";
    public static final String SELF_LEAVE_REQUEST = "SELF_LEAVE_REQUEST";
}
