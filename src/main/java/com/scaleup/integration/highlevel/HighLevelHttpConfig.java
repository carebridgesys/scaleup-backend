package com.scaleup.integration.highlevel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HighLevelHttpConfig {

    @Bean
    public RestClient highLevelRestClient(
            HighLevelProperties properties
    ) {

        return RestClient.builder()
                .baseUrl(
                        properties.getBaseUrl()
                )
                .defaultHeader(
                        "Version",
                        "v3"
                )
                .defaultHeader(
                        "Accept",
                        "application/json"
                )
                .build();
    }
}