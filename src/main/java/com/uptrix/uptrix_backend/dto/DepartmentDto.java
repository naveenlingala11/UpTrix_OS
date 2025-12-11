package com.uptrix.uptrix_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentDto {

    private Long id;
    private String name;
    private String code;
    private String status;
    private Long companyId;
}
