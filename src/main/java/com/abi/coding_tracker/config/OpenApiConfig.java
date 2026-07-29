package com.abi.coding_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI(){
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
        .info(new Info().title("Coding Tracker API").version("1.0"))
        // Tell OpenAPI that every endpoint should require this security scheme globally
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
            .addSecuritySchemes(securitySchemeName, 
                new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
            )
        );
    }
}
