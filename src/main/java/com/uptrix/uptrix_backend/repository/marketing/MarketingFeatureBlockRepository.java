package com.uptrix.uptrix_backend.repository.marketing;

import com.uptrix.uptrix_backend.entity.marketing.MarketingFeatureBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketingFeatureBlockRepository extends JpaRepository<MarketingFeatureBlock, Long> {

    List<MarketingFeatureBlock> findByPageIdOrderByPositionAsc(Long pageId);
}
