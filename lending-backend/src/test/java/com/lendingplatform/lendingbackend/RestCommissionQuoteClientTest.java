package com.lendingplatform.lendingbackend;

import com.lendingplatform.lendingbackend.config.RestTemplateConfig;
import com.lendingplatform.lendingbackend.config.VendorProperties;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteResponse;
import com.lendingplatform.lendingbackend.service.RestCommissionQuoteClient;
import com.lendingplatform.lendingbackend.service.VendorIntegrationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestCommissionQuoteClientTest {

    @Test
    void sendsApiKeyHeaderAndReturnsSuccessfulQuote() {
        VendorProperties properties = new VendorProperties();
        properties.setBaseUrl("http://localhost:9090");
        properties.setApiKey("test-api-key");

        RestTemplate restTemplate = new RestTemplateConfig(properties).restTemplate(new RestTemplateBuilder());
        RestCommissionQuoteClient client = new RestCommissionQuoteClient(restTemplate, properties);

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://localhost:9090/vendor-api/commission-quotes"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "test-api-key"))
                .andRespond(withSuccess("{\"quoteId\":\"QUOTE-123\",\"commissionRate\":0.015,\"totalCommission\":1500.00}", MediaType.APPLICATION_JSON));

        CommissionQuoteResponse response = client.requestQuote(new CommissionQuoteRequest(new BigDecimal("100000.00"), 60, "B"));

        assertThat(response.getQuoteId()).isEqualTo("QUOTE-123");
        assertThat(response.getCommissionRate()).isEqualByComparingTo(new BigDecimal("0.015"));
        assertThat(response.getTotalCommission()).isEqualByComparingTo(new BigDecimal("1500.00"));
        server.verify();
    }

    @Test
    void throwsWhenVendorApiKeyIsMissing() {
        VendorProperties properties = new VendorProperties();
        properties.setBaseUrl("http://localhost:9090");
        properties.setApiKey("");

        RestTemplate restTemplate = new RestTemplateConfig(properties).restTemplate(new RestTemplateBuilder());
        RestCommissionQuoteClient client = new RestCommissionQuoteClient(restTemplate, properties);

        assertThatThrownBy(() -> client.requestQuote(new CommissionQuoteRequest(new BigDecimal("100000.00"), 60, "B")))
                .isInstanceOf(VendorIntegrationException.class)
                .hasMessageContaining("Vendor API key is not configured");
    }
}
