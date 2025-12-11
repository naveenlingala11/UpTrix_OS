// File: uptrix-backend/src/main/java/com/uptrix/uptrix_backend/config/SpringDocConfig.java
package com.uptrix.uptrix_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .info(new Info()
                        .title("Uptrix API")
                        .version("v1")
                        .description("API documentation for Uptrix")
                        .contact(new Contact().name("Uptrix Support").email("support@uptrix.app"))
                        .license(new License().name("Proprietary").url("https://uptrix.app"))
                );
    }

    @Bean
    public OpenApiCustomizer tagCustomizer() {
        return openApi -> {
            openApi.addTagsItem(new Tag().name("Attendance").description("Attendance endpoints"));
            openApi.addTagsItem(new Tag().name("Payroll").description("Payroll and payslip endpoints"));
            openApi.addTagsItem(new Tag().name("Employees").description("Employee management"));
            openApi.addTagsItem(new Tag().name("Shifts").description("Shift management"));
            openApi.addTagsItem(new Tag().name("Admin").description("Admin / tenant management"));
        };
    }
}

/*
Changelog & instructions (summary):
- Fixed incorrect type/name `OpenApiCustomiser` -> the correct type from org.springdoc is `OpenApiCustomizer`.
- Provided a complete OpenAPI() bean that registers a JWT security scheme and basic Info metadata.
- Kept tag customizer as an OpenApiCustomizer bean.

How to apply:
1) Replace the existing SpringDocConfig.java with the above file (path: uptrix-backend/src/main/java/com/uptrix/uptrix_backend/config/SpringDocConfig.java).
2) Ensure your pom.xml contains a springdoc starter compatible with your Spring Boot version. Example for Spring Boot 3/4: `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0` is okay for many setups, but if you use different boot versions, pick the appropriate starter from Maven Central.
3) Remove or keep `StaticOpenApiController` only if you need a static YAML at `/v3/api-docs.yaml`. If you rely on springdoc auto-generated docs, it's usually not necessary.
4) Rebuild: `mvn clean package` (or `./mvnw clean package`).
5) Run the app and test:
   - JSON: http://localhost:8080/v3/api-docs
   - YAML (if you keep StaticOpenApiController or configure YAML output): http://localhost:8080/v3/api-docs.yaml
   - Swagger UI: http://localhost:8080/swagger-ui/index.html

If you still get compilation errors, copy the `mvn` build error output here and I will analyze the exact stack trace.
*/