package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.me.CurrentUserContextDto;
import com.uptrix.uptrix_backend.entity.Employee;
import com.uptrix.uptrix_backend.entity.Role;
import com.uptrix.uptrix_backend.entity.User;
import com.uptrix.uptrix_backend.repository.EmployeeRepository;
import com.uptrix.uptrix_backend.repository.UserRepository;
import com.uptrix.uptrix_backend.security.SecurityUtils;
import com.uptrix.uptrix_backend.security.PermissionMapper;
import com.uptrix.uptrix_backend.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "Current user context & profile")
public class MeController {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public MeController(UserRepository userRepository,
                        EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Operation(summary = "Get current user context",
            description = "Returns context for the logged-in user including company, employeeId, roles and permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Context returned",
                    content = @Content(schema = @Schema(implementation = CurrentUserContextDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    @GetMapping("/context")
    public ResponseEntity<CurrentUserContextDto> getContext() {

        UserPrincipal up = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));

        User user = userRepository.findById(up.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Employee emp = employeeRepository.findByUser(user).orElse(null);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        CurrentUserContextDto dto = new CurrentUserContextDto();
        dto.setUserId(user.getId());
        dto.setCompanyId(user.getCompany().getId());
        dto.setCompanyName(user.getCompany().getName());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setEmployeeId(emp != null ? emp.getId() : null);
        dto.setRoles(roleNames);
        dto.setPermissions(PermissionMapper.mapRolesToPermissions(roleNames));

        return ResponseEntity.ok(dto);
    }
}
