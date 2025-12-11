package com.uptrix.uptrix_backend.entity.platform;

import com.uptrix.uptrix_backend.entity.platform.enums.EnvironmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "environment_presets")
@Getter
@Setter
public class EnvironmentPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_settings_id", nullable = false)
    private PlatformSettings platformSettings;

    @Column(nullable = false, length = 80)
    private String name;

    // ✅ avoid MySQL reserved word "key"
    @Column(name = "env_key", nullable = false, length = 40)
    private String envKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnvironmentStatus status;

    @Column(nullable = false, length = 80)
    private String region;
}
