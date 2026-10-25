package com.lendingplatform.lendingbackend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quote-api")
public class QuoteApiProperties {

    private String apiKey = "";
    private double failureRate = 0.4;

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("LENDING_API_KEY must be configured before starting the lending backend.");
        }
        if (!Double.isFinite(failureRate) || failureRate < 0.0 || failureRate > 1.0) {
            throw new IllegalArgumentException("COMMISSION_FAILURE_RATE must be between 0.0 and 1.0.");
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
}
