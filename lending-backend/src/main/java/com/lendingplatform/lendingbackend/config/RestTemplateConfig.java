package com.lendingplatform.lendingbackend.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    private final VendorProperties vendorProperties;

    public RestTemplateConfig(VendorProperties vendorProperties) {
        this.vendorProperties = vendorProperties;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(vendorProperties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(vendorProperties.getReadTimeoutMs()))
                .build();
    }
}
