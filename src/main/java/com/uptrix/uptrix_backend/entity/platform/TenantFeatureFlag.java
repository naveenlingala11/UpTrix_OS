package com.uptrix.uptrix_backend.entity.platform;

import com.uptrix.uptrix_backend.entity.company.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "tenant_feature_flags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenant_feature", columnNames = {"company_id", "feature_key"})
        }
)
public class TenantFeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "feature_key", nullable = false, length = 50)
    private String featureKey;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

}
