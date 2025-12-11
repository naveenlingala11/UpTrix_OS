package com.uptrix.uptrix_backend.security;

import java.util.*;
import java.util.stream.Collectors;

import static com.uptrix.uptrix_backend.security.PermissionCodes.*;

public final class PermissionMapper {

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // 🔹 Super admin / platform / org-level admins → almost everything
        Set<String> superAdminPerms = Set.of(
                COMPANY_CONFIG,
                EMPLOYEE_VIEW, EMPLOYEE_MANAGE,
                DEPARTMENT_MANAGE,
                SHIFT_MANAGE,
                ATTENDANCE_VIEW_ALL,
                LEAVE_VIEW_ALL, LEAVE_APPROVE,
                HELPDESK_VIEW_ALL, HELPDESK_MANAGE
        );

        ROLE_PERMISSIONS.put("SUPER_ADMIN", superAdminPerms);
        ROLE_PERMISSIONS.put("ADMIN", superAdminPerms);
        ROLE_PERMISSIONS.put("ORG_ADMIN", superAdminPerms);

        // 🔹 IT admin – config & visibility, but not deep HR ops
        ROLE_PERMISSIONS.put("IT_ADMIN", Set.of(
                COMPANY_CONFIG,
                EMPLOYEE_VIEW,
                ATTENDANCE_VIEW_ALL,
                HELPDESK_VIEW_ALL
        ));

        // 🔹 HR family
        ROLE_PERMISSIONS.put("CHRO", Set.of(
                EMPLOYEE_VIEW, EMPLOYEE_MANAGE,
                ATTENDANCE_VIEW_ALL,
                LEAVE_VIEW_ALL, LEAVE_APPROVE,
                SHIFT_MANAGE,
                HELPDESK_VIEW_ALL
        ));

        ROLE_PERMISSIONS.put("HR", Set.of(
                EMPLOYEE_VIEW, EMPLOYEE_MANAGE,
                DEPARTMENT_MANAGE,
                ATTENDANCE_VIEW_ALL,
                LEAVE_VIEW_ALL, LEAVE_APPROVE,
                SHIFT_MANAGE,
                HELPDESK_VIEW_ALL
        ));

        ROLE_PERMISSIONS.put("HRBP", Set.of(
                EMPLOYEE_VIEW,
                ATTENDANCE_VIEW_ALL,
                LEAVE_VIEW_ALL, LEAVE_APPROVE,
                HELPDESK_VIEW_ALL
        ));

        ROLE_PERMISSIONS.put("HR_EXEC", Set.of(
                EMPLOYEE_VIEW,
                ATTENDANCE_VIEW_ALL,
                LEAVE_VIEW_ALL,
                HELPDESK_VIEW_ALL
        ));

        ROLE_PERMISSIONS.put("RECRUITER", Set.of(
                EMPLOYEE_VIEW   // for now; later: ATS permissions
        ));

        // 🔹 Managers – team-level access
        ROLE_PERMISSIONS.put("MANAGER", Set.of(
                ATTENDANCE_TEAM_VIEW,
                LEAVE_TEAM_APPROVE,
                HELPDESK_VIEW_ALL
        ));

        // 🔹 Employees – self-service
        ROLE_PERMISSIONS.put("EMPLOYEE", Set.of(
                SELF_PROFILE_VIEW,
                SELF_ATTENDANCE_VIEW,
                SELF_LEAVE_REQUEST
        ));
    }

    private PermissionMapper() {}

    public static Set<String> mapRolesToPermissions(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptySet();
        }

        return roleNames.stream()
                .flatMap(r -> ROLE_PERMISSIONS.getOrDefault(r, Collections.emptySet()).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
