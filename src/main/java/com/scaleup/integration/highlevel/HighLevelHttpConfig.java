package com.scaleup.integration.highlevel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HighLevelHttpConfig {

    @Bean
    public RestClient highLevelRestClient(
            HighLevelProperties properties
    ) {

        String baseUrl =
                requireText(
                        properties.getBaseUrl(),
                        "HighLevel base URL"
                );

        int connectTimeoutMs =
                properties
                        .getHttp()
                        .getConnectTimeoutMs();

        int readTimeoutMs =
                properties
                        .getHttp()
                        .getReadTimeoutMs();

        if (connectTimeoutMs <= 0) {
            throw new IllegalStateException(
                    "HighLevel connect timeout must be greater than zero."
            );
        }

        if (readTimeoutMs <= 0) {
            throw new IllegalStateException(
                    "HighLevel read timeout must be greater than zero."
            );
        }

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                connectTimeoutMs
        );

        requestFactory.setReadTimeout(
                readTimeoutMs
        );

        return RestClient
                .builder()
                .baseUrl(
                        baseUrl
                )
                .requestFactory(
                        requestFactory
                )
                .defaultHeader(
                        "Version",
                        "2021-07-28"
                )
                .defaultHeader(
                        "Accept",
                        "application/json"
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();
    }

    private String requireText(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalStateException(
                    fieldName
                            + " must be configured."
            );
        }

        return value.trim();
    }
}