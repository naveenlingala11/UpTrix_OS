package com.uptrix.uptrix_backend.entity.platform;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_presets")
@Getter
@Setter
public class PlanPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_settings_id", nullable = false)
    private PlatformSettings platformSettings;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "max_employees", nullable = false)
    private int maxEmployees;

    @ElementCollection
    @CollectionTable(
            name = "plan_preset_modules",
            joinColumns = @JoinColumn(name = "plan_preset_id")
    )
    @Column(name = "module_name", length = 100)
    private List<String> modules = new ArrayList<>();

    @Column(nullable = false)
    private boolean recommended;
}
