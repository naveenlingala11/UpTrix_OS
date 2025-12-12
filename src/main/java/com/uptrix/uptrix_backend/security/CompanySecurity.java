package com.uptrix.uptrix_backend.security;

import com.uptrix.uptrix_backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("companySecurity")
public class CompanySecurity {

    public boolean canAccessCompany(Authentication authentication, Long companyId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // SUPER ADMIN can see any company
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isSuperAdmin) {
            return true;
        }

        // Everyone else: only their own company
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            Long userCompanyId = user.getCompany() != null ? user.getCompany().getId() : null;
            return userCompanyId != null && userCompanyId.equals(companyId);
        }

        return false;
    }
}
