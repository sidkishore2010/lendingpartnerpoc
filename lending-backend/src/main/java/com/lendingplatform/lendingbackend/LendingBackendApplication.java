package com.lendingplatform.lendingbackend;

import com.lendingplatform.lendingbackend.config.VendorProperties;
import com.lendingplatform.lendingbackend.config.QuoteApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({VendorProperties.class, QuoteApiProperties.class})
public class LendingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LendingBackendApplication.class, args);
    }
}
