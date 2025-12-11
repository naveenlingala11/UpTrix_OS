package com.uptrix.uptrix_backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    private String fullName;
    private String username;
    private String email;
    private String password;
    private String roleName; // e.g. ADMIN, HR, MANAGER, EMPLOYEE
}
