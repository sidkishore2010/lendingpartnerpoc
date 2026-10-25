package com.lendingplatform.mockvendor.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vendor")
public class VendorApiProperties {

    private String apiKey = "";
    private double failureRate = 0.2;
    private int minDelayMs = 200;
    private int maxDelayMs = 1000;
    private Long randomSeed;

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("VENDOR_API_KEY must be configured before starting the mock vendor.");
        }
        if (failureRate < 0.0 || failureRate > 1.0) {
            throw new IllegalArgumentException("MOCK_FAILURE_RATE must be between 0.0 and 1.0.");
        }
        if (minDelayMs < 0 || maxDelayMs < 0 || maxDelayMs < minDelayMs) {
            throw new IllegalArgumentException("Mock vendor delay configuration is invalid.");
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public int getMinDelayMs() {
        return minDelayMs;
    }

    public void setMinDelayMs(int minDelayMs) {
        this.minDelayMs = minDelayMs;
    }

    public int getMaxDelayMs() {
        return maxDelayMs;
    }

    public void setMaxDelayMs(int maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
    }

    public Long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }
}
