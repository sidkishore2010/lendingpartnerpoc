package com.lendingplatform.mockvendor;

import com.lendingplatform.mockvendor.config.MockVendorBehaviorProperties;
import com.lendingplatform.mockvendor.config.VendorApiProperties;
import com.lendingplatform.mockvendor.dto.MockVendorQuoteResponse;
import com.lendingplatform.mockvendor.dto.MockVendorRequest;
import com.lendingplatform.mockvendor.service.MockVendorCommissionCalculator;
import com.lendingplatform.mockvendor.service.MockVendorQuoteService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockVendorQuoteServiceTest {

    @Test
    void calculatesCommissionUsingIllustrativeRiskRates() {
        MockVendorBehaviorProperties behavior = new MockVendorBehaviorProperties();
        behavior.setFailureRate(0.0);
        behavior.setMinDelayMs(0);
        behavior.setMaxDelayMs(0);

        VendorApiProperties vendorApiProperties = new VendorApiProperties();
        vendorApiProperties.setApiKey("test-key");

        MockVendorQuoteService service = new MockVendorQuoteService(
                behavior,
                vendorApiProperties,
                new MockVendorCommissionCalculator(),
                new Random(123L));

        MockVendorQuoteResponse response = service.generateQuote(new MockVendorRequest(new BigDecimal("100000.00"), 60, "B"));

        assertThat(response.getQuoteId()).startsWith("QUOTE-");
        assertThat(response.getCommissionRate()).isEqualByComparingTo(new BigDecimal("0.0150"));
        assertThat(response.getTotalCommission()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    void throwsWhenSimulatedFailureRateIsReached() {
        MockVendorBehaviorProperties behavior = new MockVendorBehaviorProperties();
        behavior.setFailureRate(1.0);
        behavior.setMinDelayMs(0);
        behavior.setMaxDelayMs(0);

        VendorApiProperties vendorApiProperties = new VendorApiProperties();
        vendorApiProperties.setApiKey("test-key");

        MockVendorQuoteService service = new MockVendorQuoteService(
                behavior,
                vendorApiProperties,
                new MockVendorCommissionCalculator(),
                new Random(123L));

        assertThatThrownBy(() -> service.generateQuote(new MockVendorRequest(new BigDecimal("20000.00"), 24, "A")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Simulated vendor failure");
    }
}
