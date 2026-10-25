package com.lendingplatform.lendingbackend.service;

import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteResponse;

public interface CommissionQuoteClient {
    CommissionQuoteResponse requestQuote(CommissionQuoteRequest request);
}
