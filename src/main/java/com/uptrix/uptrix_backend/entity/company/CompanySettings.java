package com.uptrix.uptrix_backend.entity.company;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "company_settings")
@Getter
@Setter
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "workday_start", length = 5)
    private String workdayStart; // "09:00"

    @Column(name = "workday_end", length = 5)
    private String workdayEnd;   // "18:00"

    @Column(name = "default_leave_types", length = 255)
    private String defaultLeaveTypes; // "PLANNED,SICK,CASUAL"

    @Column(name = "timezone_label", length = 100)
    private String timezoneLabel; // "Asia/Kolkata"
}
