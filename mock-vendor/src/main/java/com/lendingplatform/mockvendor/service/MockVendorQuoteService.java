package com.lendingplatform.mockvendor.service;

import com.lendingplatform.mockvendor.config.MockVendorBehaviorProperties;
import com.lendingplatform.mockvendor.config.VendorApiProperties;
import com.lendingplatform.mockvendor.dto.MockVendorQuoteResponse;
import com.lendingplatform.mockvendor.dto.MockVendorRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Service
public class MockVendorQuoteService {

    private final MockVendorBehaviorProperties behaviorProperties;
    private final VendorApiProperties vendorApiProperties;
    private final MockVendorCommissionCalculator calculator;
    private final Random random;

    public MockVendorQuoteService(
            MockVendorBehaviorProperties behaviorProperties,
            VendorApiProperties vendorApiProperties,
            MockVendorCommissionCalculator calculator,
            Random random) {
        this.behaviorProperties = behaviorProperties;
        this.vendorApiProperties = vendorApiProperties;
        this.calculator = calculator;
        this.random = random;
    }

    public MockVendorQuoteResponse generateQuote(MockVendorRequest request) {
        sleepForLatency();
        if (random.nextDouble() < behaviorProperties.getFailureRate()) {
            throw new IllegalStateException("Simulated vendor failure");
        }

        BigDecimal rate = calculator.calculateRate(request.getRiskBand());
        BigDecimal totalCommission = calculator.calculateTotalCommission(request.getLoanAmount(), rate);
        return new MockVendorQuoteResponse("QUOTE-" + UUID.randomUUID(), rate, totalCommission);
    }

    private void sleepForLatency() {
        int min = behaviorProperties.getMinDelayMs();
        int max = behaviorProperties.getMaxDelayMs();
        if (min <= 0 && max <= 0) {
            return;
        }
        int delayMs = min == max ? min : min + random.nextInt((max - min) + 1);
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Mock vendor latency simulation was interrupted.", e);
            }
        }
    }
}
