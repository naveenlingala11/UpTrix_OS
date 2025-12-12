package com.uptrix.uptrix_backend.dto.employee;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeResponseDto {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String workEmail;
    private String departmentName;
    private String employmentType;
    private String status;
    private LocalDate dateOfJoining;

    // Location info for UI
    private Long locationId;
    private String locationName;
}
