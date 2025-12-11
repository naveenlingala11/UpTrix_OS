package com.uptrix.uptrix_backend.dto.marketing;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MarketingPageDto {

    private String slug;
    private String title;
    private String subtitle;
    private String heroBadge;
    private String heroTitle;
    private String heroHighlight;
    private String heroDescription;
    private String primaryCtaLabel;
    private String primaryCtaLink;
    private String secondaryCtaLabel;
    private String secondaryCtaLink;
    private String badgeIconClass;
    private String theme;
    private LocalDateTime updatedAt;

    private List<MarketingFeatureDto> features;
}
