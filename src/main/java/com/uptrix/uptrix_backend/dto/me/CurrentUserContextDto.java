package com.uptrix.uptrix_backend.dto.me;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CurrentUserContextDto {

    private Long userId;
    private Long companyId;
    private String companyName;
    private String fullName;
    private String email;
    private Long employeeId;

    private Set<String> roles;        // e.g. ["SUPER_ADMIN","ADMIN"]
    private Set<String> permissions;  // e.g. ["COMPANY_CONFIG","EMPLOYEE_MANAGE"]
}
