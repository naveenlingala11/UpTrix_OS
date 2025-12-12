package com.uptrix.uptrix_backend.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyLocationDto {

    private Long id;
    private Long companyId;
    private String code;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private boolean active;
}
