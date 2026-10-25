package com.lendingplatform.lendingbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.lendingbackend.config.QuoteApiProperties;
import com.lendingplatform.lendingbackend.config.QuoteApiKeyInterceptor;
import com.lendingplatform.lendingbackend.config.QuoteApiKeyWebConfig;
import com.lendingplatform.lendingbackend.controller.ApiExceptionHandler;
import com.lendingplatform.lendingbackend.controller.CommissionQuoteController;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteResponse;
import com.lendingplatform.lendingbackend.service.CommissionApiException;
import com.lendingplatform.lendingbackend.service.CommissionQuoteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommissionQuoteController.class)
@Import({ApiExceptionHandler.class, QuoteApiKeyInterceptor.class, QuoteApiKeyWebConfig.class})
class CommissionQuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommissionQuoteService commissionQuoteService;

    @MockBean
    private QuoteApiProperties quoteApiProperties;

    @org.junit.jupiter.api.BeforeEach
    void configureApiKey() {
        Mockito.when(quoteApiProperties.getApiKey()).thenReturn("test-lending-key");
        Mockito.when(quoteApiProperties.getFailureRate()).thenReturn(0.0);
    }

    @Test
    void successfulQuoteRequestReturnsQuote() throws Exception {
        CommissionQuoteResponse response = new CommissionQuoteResponse("QUOTE-10001", new BigDecimal("0.015"), new BigDecimal("1500.00"));
        Mockito.when(commissionQuoteService.generateQuote(any(CommissionQuoteRequest.class))).thenReturn(response);

        CommissionQuoteRequest request = new CommissionQuoteRequest(new BigDecimal("100000.00"), 60, "B");

        mockMvc.perform(post("/api/commission-quotes")
                        .header("api-key", "test-lending-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").value("QUOTE-10001"))
                .andExpect(jsonPath("$.commissionRate").value(0.015))
                .andExpect(jsonPath("$.totalCommission").value(1500.00));
    }

    @Test
    void invalidRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/commission-quotes")
                        .header("api-key", "test-lending-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loanAmount\":0,\"loanTermInMonths\":0,\"riskBand\":\"Z\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingApiKeyReturns401() throws Exception {
        mockMvc.perform(post("/api/commission-quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loanAmount\":100000,\"loanTermInMonths\":60,\"riskBand\":\"B\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("API_KEY_INVALID"));
    }

    @Test
    void incorrectApiKeyReturns401() throws Exception {
        mockMvc.perform(post("/api/commission-quotes")
                        .header("api-key", "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loanAmount\":100000,\"loanTermInMonths\":60,\"riskBand\":\"B\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("API_KEY_INVALID"));
    }

    @Test
    void missingApiKeyIsRejectedBeforeMalformedRequestBody() throws Exception {
        mockMvc.perform(post("/api/commission-quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("API_KEY_INVALID"));
    }

    @Test
    void simulatedServiceFailureReturns503() throws Exception {
        Mockito.when(commissionQuoteService.generateQuote(any(CommissionQuoteRequest.class)))
                .thenThrow(new CommissionApiException(
                        "COMMISSION_QUOTE_TEMPORARY_FAILURE",
                        "The commission quote service is temporarily unavailable. Please try again.",
                        HttpStatus.SERVICE_UNAVAILABLE));

        mockMvc.perform(post("/api/commission-quotes")
                        .header("api-key", "test-lending-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loanAmount\":100000,\"loanTermInMonths\":60,\"riskBand\":\"B\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("COMMISSION_QUOTE_TEMPORARY_FAILURE"));
    }
}
