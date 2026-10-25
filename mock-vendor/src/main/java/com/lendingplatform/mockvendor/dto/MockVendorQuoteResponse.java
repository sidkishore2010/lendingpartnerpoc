package com.lendingplatform.mockvendor.dto;

import java.math.BigDecimal;

public class MockVendorQuoteResponse {

    private String quoteId;
    private BigDecimal commissionRate;
    private BigDecimal totalCommission;

    public MockVendorQuoteResponse() {
    }

    public MockVendorQuoteResponse(String quoteId, BigDecimal commissionRate, BigDecimal totalCommission) {
        this.quoteId = quoteId;
        this.commissionRate = commissionRate;
        this.totalCommission = totalCommission;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }
}
