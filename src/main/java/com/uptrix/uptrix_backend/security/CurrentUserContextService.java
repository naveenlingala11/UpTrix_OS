package com.uptrix.uptrix_backend.security;

import com.uptrix.uptrix_backend.dto.context.CurrentUserContext;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserContextService {

    private final UserRepository userRepository;

    public CurrentUserContextService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CurrentUserContext get() {
        var principalOpt = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));

        User user = userRepository.findById(principalOpt.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        CurrentUserContext ctx = new CurrentUserContext();
        ctx.setUserId(user.getId());
        ctx.setCompanyId(user.getCompany().getId());
        ctx.setCompanyName(user.getCompany().getName());
        ctx.setFullName(user.getFullName());
        ctx.setEmail(user.getEmail());
        ctx.setEmployeeId(
                user.getEmployee() != null ? user.getEmployee().getId() : null
        );
        ctx.setRoles(
                user.getRoles().stream().map(r -> r.getName()).toList()
        );
        ctx.setPermissions(
                user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getCode())
                        .distinct()
                        .toList()
        );
        return ctx;
    }
}
