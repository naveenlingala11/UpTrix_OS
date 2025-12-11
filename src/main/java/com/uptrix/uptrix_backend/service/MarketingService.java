package com.uptrix.uptrix_backend.service;

import com.uptrix.uptrix_backend.dto.marketing.MarketingFeatureDto;
import com.uptrix.uptrix_backend.dto.marketing.MarketingPageDto;
import com.uptrix.uptrix_backend.entity.marketing.MarketingFeatureBlock;
import com.uptrix.uptrix_backend.entity.marketing.MarketingPage;
import com.uptrix.uptrix_backend.repository.marketing.MarketingPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class MarketingService {

    private final MarketingPageRepository pageRepository;

    public MarketingService(MarketingPageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Transactional(readOnly = true)
    public MarketingPageDto getPageBySlug(String slug) {
        MarketingPage page = pageRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Marketing page not found: " + slug));

        MarketingPageDto dto = new MarketingPageDto();
        dto.setSlug(page.getSlug());
        dto.setTitle(page.getTitle());
        dto.setSubtitle(page.getSubtitle());
        dto.setHeroBadge(page.getHeroBadge());
        dto.setHeroTitle(page.getHeroTitle());
        dto.setHeroHighlight(page.getHeroHighlight());
        dto.setHeroDescription(page.getHeroDescription());
        dto.setPrimaryCtaLabel(page.getPrimaryCtaLabel());
        dto.setPrimaryCtaLink(page.getPrimaryCtaLink());
        dto.setSecondaryCtaLabel(page.getSecondaryCtaLabel());
        dto.setSecondaryCtaLink(page.getSecondaryCtaLink());
        dto.setBadgeIconClass(page.getBadgeIconClass());
        dto.setTheme(page.getTheme());
        dto.setUpdatedAt(page.getUpdatedAt());

        dto.setFeatures(
                page.getFeatures().stream()
                        .map(this::toFeatureDto)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private MarketingFeatureDto toFeatureDto(MarketingFeatureBlock f) {
        MarketingFeatureDto dto = new MarketingFeatureDto();
        dto.setTitle(f.getTitle());
        dto.setDescription(f.getDescription());
        dto.setSectionKey(f.getSectionKey());
        dto.setIconClass(f.getIconClass());
        dto.setPosition(f.getPosition());
        dto.setBullet1(f.getBullet1());
        dto.setBullet2(f.getBullet2());
        dto.setBullet3(f.getBullet3());
        return dto;
    }
}
