package com.lendingplatform.mockvendor.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
@EnableConfigurationProperties(MockVendorBehaviorProperties.class)
public class MockVendorConfiguration {

    @Bean
    public Random mockVendorRandom(MockVendorBehaviorProperties properties) {
        properties.validate();
        if (properties.getRandomSeed() != null) {
            return new Random(properties.getRandomSeed());
        }
        return new Random();
    }
}
