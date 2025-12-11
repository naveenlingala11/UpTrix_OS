package com.uptrix.uptrix_backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private boolean active;
    private String primaryRole;
}
