package com.nivasafinance.features.document.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        DocumentStorageProperties.class,
        S3Properties.class,
        LocalStorageProperties.class
})
public class DocumentConfiguration {
}


