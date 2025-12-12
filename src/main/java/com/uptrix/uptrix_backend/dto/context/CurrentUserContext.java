package com.uptrix.uptrix_backend.dto.context;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CurrentUserContext {
    private Long userId;
    private Long companyId;
    private String companyName;
    private String fullName;
    private String email;
    private Long employeeId;
    private List<String> roles;
    private List<String> permissions;
}
