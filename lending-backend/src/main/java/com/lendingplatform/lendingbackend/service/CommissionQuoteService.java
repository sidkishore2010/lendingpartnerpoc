package com.lendingplatform.lendingbackend.service;

import com.lendingplatform.lendingbackend.config.QuoteApiProperties;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteResponse;
import com.lendingplatform.lendingbackend.validation.CommissionQuoteValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class CommissionQuoteService {

    private final CommissionQuoteClient commissionQuoteClient;
    private final CommissionQuoteValidator validator;
    private final QuoteApiProperties quoteApiProperties;

    public CommissionQuoteService(
            CommissionQuoteClient commissionQuoteClient,
            CommissionQuoteValidator validator,
            QuoteApiProperties quoteApiProperties) {
        this.commissionQuoteClient = commissionQuoteClient;
        this.validator = validator;
        this.quoteApiProperties = quoteApiProperties;
    }

    public CommissionQuoteResponse generateQuote(CommissionQuoteRequest request) {
        validator.validateRequest(request);
        if (ThreadLocalRandom.current().nextDouble() < quoteApiProperties.getFailureRate()) {
            throw new CommissionApiException(
                    "COMMISSION_QUOTE_TEMPORARY_FAILURE",
                    "The commission quote service is temporarily unavailable. Please try again.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        return commissionQuoteClient.requestQuote(request);
    }
}
