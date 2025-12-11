package com.uptrix.uptrix_backend.repository.marketing;

import com.uptrix.uptrix_backend.entity.marketing.MarketingPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketingPageRepository extends JpaRepository<MarketingPage, Long> {

    Optional<MarketingPage> findBySlug(String slug);
}
