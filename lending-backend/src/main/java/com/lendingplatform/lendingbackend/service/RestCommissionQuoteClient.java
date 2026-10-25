package com.lendingplatform.lendingbackend.service;

import com.lendingplatform.lendingbackend.config.VendorProperties;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteRequest;
import com.lendingplatform.lendingbackend.dto.CommissionQuoteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class RestCommissionQuoteClient implements CommissionQuoteClient {

    private static final Logger log = LoggerFactory.getLogger(RestCommissionQuoteClient.class);
    private static final String VENDOR_ENDPOINT = "/vendor-api/commission-quotes";

    private final RestTemplate restTemplate;
    private final VendorProperties vendorProperties;

    public RestCommissionQuoteClient(RestTemplate restTemplate, VendorProperties vendorProperties) {
        this.restTemplate = restTemplate;
        this.vendorProperties = vendorProperties;
    }

    @Override
    public CommissionQuoteResponse requestQuote(CommissionQuoteRequest request) {
        validateConfiguration();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", vendorProperties.getApiKey());

        HttpEntity<CommissionQuoteRequest> entity = new HttpEntity<>(request, headers);
        String url = vendorProperties.getBaseUrl().replaceAll("/+$", "") + VENDOR_ENDPOINT;

        try {
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, CommissionQuoteResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new VendorIntegrationException("VENDOR_INVALID_RESPONSE", "The commission vendor returned an invalid response.", HttpStatus.BAD_GATEWAY);
            }
            CommissionQuoteResponse body = response.getBody();
            if (body.getQuoteId() == null || body.getCommissionRate() == null || body.getTotalCommission() == null) {
                throw new VendorIntegrationException("VENDOR_MALFORMED_RESPONSE", "The commission vendor response is missing required fields.", HttpStatus.BAD_GATEWAY);
            }
            return body;
        } catch (ResourceAccessException ex) {
            log.warn("Vendor request timed out or connection failed", ex);
            Throwable cause = ex.getCause();
            if (cause instanceof java.net.SocketTimeoutException || cause instanceof java.util.concurrent.TimeoutException) {
                throw new VendorIntegrationException("VENDOR_TIMEOUT", "The commission service took too long to respond.", HttpStatus.GATEWAY_TIMEOUT);
            }
            throw new VendorIntegrationException("VENDOR_UNAVAILABLE", "The commission service is unavailable.", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (HttpClientErrorException ex) {
            String message = "The commission service rejected the request.";
            if (ex.getStatusCode().value() == 400) {
                throw new VendorIntegrationException("VENDOR_BAD_REQUEST", message, HttpStatus.BAD_GATEWAY);
            }
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new VendorIntegrationException("VENDOR_AUTH_FAILED", "The commission service rejected the vendor credentials.", HttpStatus.SERVICE_UNAVAILABLE);
            }
            if (ex.getStatusCode().value() == 429) {
                throw new VendorIntegrationException("VENDOR_RATE_LIMITED", "The commission service is temporarily rate limited.", HttpStatus.SERVICE_UNAVAILABLE);
            }
            throw new VendorIntegrationException("VENDOR_BAD_REQUEST", message, HttpStatus.BAD_GATEWAY);
        } catch (HttpServerErrorException ex) {
            log.error("Vendor returned server error: {}", ex.getStatusCode(), ex);
            throw new VendorIntegrationException("VENDOR_SERVER_ERROR", "The commission service failed while generating a quote.", HttpStatus.BAD_GATEWAY);
        } catch (RestClientException ex) {
            log.warn("Vendor response could not be parsed", ex);
            throw new VendorIntegrationException("VENDOR_MALFORMED_RESPONSE", "The commission service returned an unreadable response.", HttpStatus.BAD_GATEWAY);
        }
    }

    private void validateConfiguration() {
        if (vendorProperties == null || vendorProperties.getBaseUrl() == null || vendorProperties.getBaseUrl().isBlank()) {
            throw new VendorIntegrationException("VENDOR_CONFIGURATION_ERROR", "Vendor base URL is not configured.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (vendorProperties.getApiKey() == null || vendorProperties.getApiKey().isBlank()) {
            throw new VendorIntegrationException("VENDOR_API_KEY_MISSING", "Vendor API key is not configured.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
