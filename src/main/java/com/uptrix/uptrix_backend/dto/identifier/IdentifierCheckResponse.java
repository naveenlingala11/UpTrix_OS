package com.uptrix.uptrix_backend.dto.identifier;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentifierCheckResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String username;

    private Long companyId;
    private String companyName;

    private String primaryRole;
    private boolean active;
}