package com.lendingplatform.lendingbackend;

import com.lendingplatform.lendingbackend.config.QuoteApiProperties;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import com.lendingplatform.lendingbackend.service.CommissionApiException;
import com.lendingplatform.lendingbackend.service.CommissionQuoteClient;
import com.lendingplatform.lendingbackend.service.CommissionQuoteService;
import com.lendingplatform.lendingbackend.validation.CommissionQuoteValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CommissionQuoteServiceTest {

    @Test
    void failureRateOfOneAlwaysFailsBeforeCallingVendor() {
        CommissionQuoteClient client = mock(CommissionQuoteClient.class);
        CommissionQuoteValidator validator = new CommissionQuoteValidator();
        QuoteApiProperties properties = new QuoteApiProperties();
        properties.setFailureRate(1.0);
        CommissionQuoteService service = new CommissionQuoteService(client, validator, properties);
        CommissionQuoteRequest request = new CommissionQuoteRequest(new BigDecimal("100000.00"), 60, "B");

        assertThatThrownBy(() -> service.generateQuote(request))
                .isInstanceOf(CommissionApiException.class)
                .hasMessageContaining("temporarily unavailable")
                .extracting("code")
                .isEqualTo("COMMISSION_QUOTE_TEMPORARY_FAILURE");
        verify(client, never()).requestQuote(request);
    }
}
