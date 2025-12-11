package com.uptrix.uptrix_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private Long companyId;
    private String usernameOrEmail;
    private String password;
}
