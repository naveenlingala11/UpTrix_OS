package com.uptrix.uptrix_backend.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final Long employeeId;
    private final String username;
    private final String password;
    private final Set<String> roles;

    private UserPrincipal(Long userId,
                          Long employeeId,
                          String username,
                          String password,
                          Set<String> roles) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public static UserPrincipal of(Long userId,
                                   Long employeeId,
                                   String username,
                                   String password,
                                   Set<String> roles) {
        return new UserPrincipal(userId, employeeId, username, password, roles);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public boolean hasRole(String role) {
        if (role == null) return false;
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }


    // ---- NEW HELPERS (to fix your errors) ----

    /** Alias for userId – used in some helper classes */
    public Long getId() {
        return userId;
    }

    /** Return raw role names – used in AuthChecks */
    public Set<String> getRoleNames() {
        return roles;
    }

    /** Optional more generic name, if you want to use it */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * For now, we don't have permissions on principal.
     * Return empty set so code compiles; later we will wire real permissions.
     */
    public Set<String> getPermissions() {
        return Set.of();
    }
}
