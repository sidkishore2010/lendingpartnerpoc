package com.lendingplatform.lendingbackend.validation;

import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CommissionQuoteValidator {

    public void validateRequest(CommissionQuoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getLoanAmount() == null) {
            throw new IllegalArgumentException("Loan amount is required.");
        }
        if (request.getLoanTermInMonths() == null) {
            throw new IllegalArgumentException("Loan term is required.");
        }
        if (request.getRiskBand() == null || !request.getRiskBand().matches("[ABCD]")) {
            throw new IllegalArgumentException("Risk band must be A, B, C, or D.");
        }
        if (request.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero.");
        }
        if (request.getLoanAmount().compareTo(new BigDecimal("10000000")) > 0) {
            throw new IllegalArgumentException("Loan amount must be no more than 10,000,000.");
        }
        if (request.getLoanTermInMonths() < 1 || request.getLoanTermInMonths() > 360) {
            throw new IllegalArgumentException("Loan term must be between 1 and 360 months.");
        }
    }
}
