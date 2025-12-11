package com.uptrix.uptrix_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Setter
@Getter
@Table(
        name = "shifts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"code"})
        }
)
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "grace_minutes")
    private Integer graceMinutes;

    @Column(name = "is_night_shift", nullable = false)
    private Boolean nightShift = Boolean.FALSE;

    /**
     * If TRUE, payroll/night-allowance logic can pick this up automatically.
     */
    @Column(name = "auto_night_allowance", nullable = false)
    private Boolean autoNightAllowance = Boolean.FALSE;

    /**
     * Optional flat allowance amount (for future payroll integration).
     */
    @Column(name = "night_allowance_amount", precision = 12, scale = 2)
    private BigDecimal nightAllowanceAmount;

    /**
     * Rotational group identifier, e.g. "WEEKLY_ROTATION_A".
     */
    @Column(name = "rotation_group", length = 100)
    private String rotationGroup;

    /**
     * Order within the rotation group (1,2,3...).
     */
    @Column(name = "rotation_order")
    private Integer rotationOrder;

    /**
     * Latitude for this shift's office/location center.
     */
    @Column(name = "geo_latitude")
    private Double geoLatitude;

    /**
     * Longitude for this shift's office/location center.
     */
    @Column(name = "geo_longitude")
    private Double geoLongitude;

    /**
     * Allowed radius in meters around (lat, lng) for geo-fenced attendance.
     */
    @Column(name = "geo_radius_meters")
    private Integer geoRadiusMeters;

    // ACTIVE / INACTIVE
    @Column(length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.nightShift == null) {
            this.nightShift = Boolean.FALSE;
        }
        if (this.autoNightAllowance == null) {
            this.autoNightAllowance = Boolean.FALSE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
