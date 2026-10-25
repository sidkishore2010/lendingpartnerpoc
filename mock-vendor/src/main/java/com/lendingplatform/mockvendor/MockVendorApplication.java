package com.lendingplatform.mockvendor;

import com.lendingplatform.mockvendor.config.VendorApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(VendorApiProperties.class)
public class MockVendorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockVendorApplication.class, args);
    }
}
