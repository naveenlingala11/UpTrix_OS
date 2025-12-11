package com.uptrix.uptrix_backend.controller;

import com.uptrix.uptrix_backend.dto.marketing.MarketingPageDto;
import com.uptrix.uptrix_backend.service.MarketingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/marketing/pages")
@Tag(name = "Marketing", description = "Public marketing pages")
public class MarketingController {

    private final MarketingService marketingService;

    public MarketingController(MarketingService marketingService) {
        this.marketingService = marketingService;
    }

    @Operation(summary = "Get marketing page by slug",
            description = "Returns marketing content for the given slug")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page found",
                    content = @Content(schema = @Schema(implementation = MarketingPageDto.class))),
            @ApiResponse(responseCode = "404", description = "Page not found")
    })
    @GetMapping("/{slug}")
    public ResponseEntity<MarketingPageDto> getPage(@PathVariable String slug) {
        MarketingPageDto dto = marketingService.getPageBySlug(slug);
        return ResponseEntity.ok(dto);
    }
}
