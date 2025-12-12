package com.uptrix.uptrix_backend.repository.platform;

import com.uptrix.uptrix_backend.entity.platform.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, Long> {
    // single-row, so default JpaRepository methods are enough
}
