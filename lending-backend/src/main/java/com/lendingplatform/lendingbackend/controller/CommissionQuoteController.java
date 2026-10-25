package com.lendingplatform.lendingbackend.controller;

import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteResponse;
import com.lendingplatform.lendingbackend.service.CommissionQuoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommissionQuoteController {

    private final CommissionQuoteService commissionQuoteService;

    public CommissionQuoteController(CommissionQuoteService commissionQuoteService) {
        this.commissionQuoteService = commissionQuoteService;
    }

    @PostMapping("/commission-quotes")
    public ResponseEntity<CommissionQuoteResponse> generateQuote(@Valid @RequestBody CommissionQuoteRequest request) {
        return ResponseEntity.ok(commissionQuoteService.generateQuote(request));
    }
}
