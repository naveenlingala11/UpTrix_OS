package com.uptrix.uptrix_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private Long companyId;
    private String companyName;
    private String primaryRole;

    // link to Employee record for ESS / helpdesk
    private Long employeeId;
}
