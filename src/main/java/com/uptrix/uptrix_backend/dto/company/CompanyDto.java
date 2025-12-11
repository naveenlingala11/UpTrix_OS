package com.uptrix.uptrix_backend.dto.company;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CompanyDto {

    private Long id;
    private String name;
    private String legalName;
    private String subdomain;
    private String status;
    private LocalDateTime createdAt;
}
