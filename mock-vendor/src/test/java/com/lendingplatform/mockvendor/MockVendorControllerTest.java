package com.lendingplatform.mockvendor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.mockvendor.config.MockVendorBehaviorProperties;
import com.lendingplatform.mockvendor.config.VendorApiProperties;
import com.lendingplatform.mockvendor.controller.MockVendorController;
import com.lendingplatform.mockvendor.dto.MockVendorRequest;
import com.lendingplatform.mockvendor.service.MockVendorCommissionCalculator;
import com.lendingplatform.mockvendor.service.MockVendorQuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockVendorController.class)
class MockVendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MockVendorQuoteService mockVendorQuoteService;

    @MockBean
    private VendorApiProperties vendorApiProperties;

    @Test
    void validRequestReturnsQuote() throws Exception {
        when(vendorApiProperties.getApiKey()).thenReturn("correct-key");
        when(mockVendorQuoteService.generateQuote(any(MockVendorRequest.class)))
                .thenReturn(new com.lendingplatform.mockvendor.dto.MockVendorQuoteResponse("QUOTE-10001", new BigDecimal("0.0150"), new BigDecimal("1500.00")));

        MockVendorRequest request = new MockVendorRequest(new BigDecimal("100000.00"), 60, "B");

        mockMvc.perform(post("/vendor-api/commission-quotes")
                        .header("api-key", "correct-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteId").value("QUOTE-10001"));
    }

    @Test
    void invalidApiKeyIsRejected() throws Exception {
        when(vendorApiProperties.getApiKey()).thenReturn("correct-key");

        MockVendorRequest request = new MockVendorRequest(new BigDecimal("100000.00"), 60, "B");

        mockMvc.perform(post("/vendor-api/commission-quotes")
                        .header("api-key", "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
