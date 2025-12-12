package com.uptrix.uptrix_backend.security;

public final class AuthChecks {

    private AuthChecks() {}

    public static boolean hasRole(String roleName) {
        return SecurityUtils.getCurrentUser()
                .map(up -> up.getRoleNames().contains(roleName))
                .orElse(false);
    }

    public static boolean hasPermission(String permission) {
        // if you add permissions to UserPrincipal later
        return SecurityUtils.getCurrentUser()
                .map(up -> up.getPermissions().contains(permission))
                .orElse(false);
    }
}
