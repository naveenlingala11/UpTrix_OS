package com.uptrix.uptrix_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfProfileDto {

    // User info
    private Long userId;
    private String fullName;
    private String email;
    private String primaryRole;

    // Company info
    private Long companyId;
    private String companyName;

    // Employee info (HR profile)
    private Long employeeId;
    private String employeeCode;
    private String departmentName;
    private String employmentType;
    private String status;
}
