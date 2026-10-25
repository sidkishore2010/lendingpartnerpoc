package com.lendingplatform.mockvendor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mock.vendor")
public class MockVendorBehaviorProperties {

    private double failureRate = 0.2;
    private int minDelayMs = 200;
    private int maxDelayMs = 1000;
    private Long randomSeed;

    public void validate() {
        if (failureRate < 0.0 || failureRate > 1.0) {
            throw new IllegalArgumentException("Failure rate must be between 0.0 and 1.0");
        }
        if (minDelayMs < 0 || maxDelayMs < 0 || maxDelayMs < minDelayMs) {
            throw new IllegalArgumentException("Delay configuration is invalid");
        }
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
