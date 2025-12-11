package com.uptrix.uptrix_backend.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaticOpenApiController {
    @GetMapping(value = "/v3/api-docs.yaml")
    public ResponseEntity<Resource> openApiYaml() {
        Resource resource = new ClassPathResource("openapi.yml");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-yaml"));
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
