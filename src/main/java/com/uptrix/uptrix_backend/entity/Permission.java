package com.uptrix.uptrix_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. EMPLOYEE_VIEW, EMPLOYEE_CREATE, LEAVE_APPROVE
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 200)
    private String description;
}
