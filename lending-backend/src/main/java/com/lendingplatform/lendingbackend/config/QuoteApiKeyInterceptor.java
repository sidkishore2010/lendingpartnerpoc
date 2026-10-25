package com.lendingplatform.lendingbackend.config;

import com.lendingplatform.lendingbackend.service.CommissionApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class QuoteApiKeyInterceptor implements HandlerInterceptor {

    private final QuoteApiProperties quoteApiProperties;

    public QuoteApiKeyInterceptor(QuoteApiProperties quoteApiProperties) {
        this.quoteApiProperties = quoteApiProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String apiKey = request.getHeader("api-key");
        String expectedApiKey = quoteApiProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank() || expectedApiKey == null || expectedApiKey.isBlank()
                || !MessageDigest.isEqual(
                        apiKey.getBytes(StandardCharsets.UTF_8),
                        expectedApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new CommissionApiException(
                    "API_KEY_INVALID",
                    "A valid api-key header is required.",
                    HttpStatus.UNAUTHORIZED);
        }
        return true;
    }
}
