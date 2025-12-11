package com.uptrix.uptrix_backend.dto.employee;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeCreateRequest {

    private String employeeCode;
    private String firstName;
    private String lastName;
    private String workEmail;
    private Long departmentId;      // optional
    private LocalDate dateOfJoining;
    private String employmentType;  // FULL_TIME, INTERN, CONTRACT

    // Location selection from UI (optional)
    private Long locationId;
}
