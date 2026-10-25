package com.lendingplatform.mockvendor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class MockVendorRequest {

    @NotNull(message = "Loan amount is required.")
    @DecimalMin(value = "0.01", message = "Loan amount must be greater than zero.")
    @Digits(integer = 10, fraction = 2, message = "Loan amount can have up to 2 decimal places.")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan term is required.")
    @Min(value = 1, message = "Loan term must be at least 1.")
    @Max(value = 360, message = "Loan term must be less than or equal to 360.")
    private Integer loanTermInMonths;

    @NotNull(message = "Risk band is required.")
    @Pattern(regexp = "^[ABCD]$", message = "Risk band must be A, B, C, or D.")
    private String riskBand;

    public MockVendorRequest() {
    }

    public MockVendorRequest(BigDecimal loanAmount, Integer loanTermInMonths, String riskBand) {
        this.loanAmount = loanAmount;
        this.loanTermInMonths = loanTermInMonths;
        this.riskBand = riskBand;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Integer getLoanTermInMonths() {
        return loanTermInMonths;
    }

    public void setLoanTermInMonths(Integer loanTermInMonths) {
        this.loanTermInMonths = loanTermInMonths;
    }

    public String getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(String riskBand) {
        this.riskBand = riskBand;
    }
}
