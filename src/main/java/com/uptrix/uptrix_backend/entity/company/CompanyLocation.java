package com.uptrix.uptrix_backend.entity.company;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "company_locations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_company_location_code", columnNames = {"company_id", "code"})
        }
)
@Getter
@Setter
public class CompanyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // parent company
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 50)
    private String code; // e.g. BLR-HSR, HYD-MAD

    @Column(nullable = false, length = 150)
    private String name; // e.g. "Bangalore - HSR Layout"

    @Column(length = 250)
    private String addressLine1;

    @Column(length = 250)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String pincode;

    @Column(length = 100)
    private String country;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
