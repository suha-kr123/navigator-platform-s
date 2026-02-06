package com.nivasafinance.redash.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RedashConfig {

    @Value("${redash.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor redashApiKeyInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.query("api_key", apiKey);
            }
        };
    }
}

