package com.scaleup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig
        implements WebMvcConfigurer {

    private final String frontendOrigin;

    public CorsConfig(
            @Value(
                    "${app.cors.public-frontend-origin:http://localhost:5173}"
            )
            String frontendOrigin
    ) {

        this.frontendOrigin =
                frontendOrigin;
    }

    @Override
    public void addCorsMappings(
            CorsRegistry registry
    ) {

        registry
                .addMapping(
                        "/api/public/**"
                )
                .allowedOrigins(
                        frontendOrigin
                )
                .allowedMethods(
                        "GET",
                        "POST",
                        "OPTIONS"
                )
                .allowedHeaders(
                        "Content-Type"
                )
                .allowCredentials(
                        false
                )
                .maxAge(
                        3600
                );
    }
}