package com.uptrix.uptrix_backend.dto.identifier;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdentifierCheckRequest {

    private Long companyId;
    private String usernameOrEmail;

}
