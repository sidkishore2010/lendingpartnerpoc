package com.lendingplatform.lendingbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class QuoteApiKeyWebConfig implements WebMvcConfigurer {

    private final QuoteApiKeyInterceptor quoteApiKeyInterceptor;

    public QuoteApiKeyWebConfig(QuoteApiKeyInterceptor quoteApiKeyInterceptor) {
        this.quoteApiKeyInterceptor = quoteApiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(quoteApiKeyInterceptor)
                .addPathPatterns("/api/commission-quotes");
    }
}
