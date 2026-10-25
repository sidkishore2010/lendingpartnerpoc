package com.lendingplatform.mockvendor.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MockVendorCommissionCalculator {

    public BigDecimal calculateRate(String riskBand) {
        return switch (riskBand) {
            case "A" -> new BigDecimal("0.0100");
            case "B" -> new BigDecimal("0.0150");
            case "C" -> new BigDecimal("0.0200");
            case "D" -> new BigDecimal("0.0250");
            default -> throw new IllegalArgumentException("Risk band must be A, B, C, or D.");
        };
    }

    public BigDecimal calculateTotalCommission(BigDecimal loanAmount, BigDecimal rate) {
        return loanAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
