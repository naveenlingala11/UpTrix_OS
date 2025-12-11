package com.uptrix.uptrix_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAdminRequest {

    // Company info
    private String companyName;
    private String legalName;
    private String subdomain;

    // Admin user info
    private String fullName;
    private String email;
    private String username;
    private String password;
}
