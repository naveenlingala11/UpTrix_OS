package com.uptrix.uptrix_backend.entity.marketing;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "marketing_feature_blocks")
@Getter
@Setter
public class MarketingFeatureBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Main heading of block
    @Column(nullable = false, length = 150)
    private String title;

    // Small description paragraph
    @Column(columnDefinition = "TEXT")
    private String description;

    // Section tag, e.g. "hero-features", "why-uptrix"
    @Column(length = 100)
    private String sectionKey;

    // Icon class for FontAwesome
    @Column(length = 80)
    private String iconClass;

    // For ordering
    private Integer position;

    // Simple 3 bullet points
    @Column(length = 200)
    private String bullet1;

    @Column(length = 200)
    private String bullet2;

    @Column(length = 200)
    private String bullet3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private MarketingPage page;
}
