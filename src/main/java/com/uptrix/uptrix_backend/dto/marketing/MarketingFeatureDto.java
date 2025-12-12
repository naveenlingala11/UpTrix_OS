package com.uptrix.uptrix_backend.dto.marketing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketingFeatureDto {

    private String title;
    private String description;
    private String sectionKey;
    private String iconClass;
    private Integer position;
    private String bullet1;
    private String bullet2;
    private String bullet3;
}
