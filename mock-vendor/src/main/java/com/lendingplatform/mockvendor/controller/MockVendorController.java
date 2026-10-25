package com.lendingplatform.mockvendor.controller;

import com.lendingplatform.mockvendor.config.VendorApiProperties;
import com.lendingplatform.mockvendor.dto.MockVendorQuoteResponse;
import com.lendingplatform.mockvendor.dto.MockVendorRequest;
import com.lendingplatform.mockvendor.service.MockVendorQuoteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MockVendorController {

    private static final Logger log = LoggerFactory.getLogger(MockVendorController.class);

    private final MockVendorQuoteService mockVendorQuoteService;
    private final VendorApiProperties vendorApiProperties;

    public MockVendorController(MockVendorQuoteService mockVendorQuoteService, VendorApiProperties vendorApiProperties) {
        this.mockVendorQuoteService = mockVendorQuoteService;
        this.vendorApiProperties = vendorApiProperties;
    }

    @PostMapping("/vendor-api/commission-quotes")
    public ResponseEntity<MockVendorQuoteResponse> generateQuote(
            @RequestHeader(value = "api-key", required = false) String apiKey,
            @Valid @RequestBody MockVendorRequest request) {
        if (apiKey == null || apiKey.isBlank() || !apiKey.equals(vendorApiProperties.getApiKey())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing api-key header.");
        }

        try {
            MockVendorQuoteResponse response = mockVendorQuoteService.generateQuote(request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException ex) {
            log.warn("Mock vendor simulated failure: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated vendor failure.");
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }
}
