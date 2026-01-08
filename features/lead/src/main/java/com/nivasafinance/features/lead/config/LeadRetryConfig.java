package com.nivasafinance.features.lead.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class LeadRetryConfig {
    // Configuration class to enable Spring Retry for optimistic locking conflicts
}

