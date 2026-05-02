package com.sbadss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * RestTemplate bean for calling the Python AI microservice.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
