package com.uptrix.uptrix_backend.entity.marketing;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "marketing_pages")
public class MarketingPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // hr-software, payroll, hiring-onboarding, etc.
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @Column(length = 120)
    private String heroBadge;

    @Column(length = 250)
    private String heroTitle;

    @Column(length = 200)
    private String heroHighlight;

    @Column(columnDefinition = "TEXT")
    private String heroDescription;

    @Column(length = 100)
    private String primaryCtaLabel;

    @Column(length = 200)
    private String primaryCtaLink;

    @Column(length = 100)
    private String secondaryCtaLabel;

    @Column(length = 200)
    private String secondaryCtaLink;

    @Column(length = 100)
    private String badgeIconClass;

    @Column(length = 20)
    private String theme; // "hr", "payroll", etc.

    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "page",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("position ASC")
    private List<MarketingFeatureBlock> features = new ArrayList<>();
}
