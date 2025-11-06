package com.nivasafinance.features.document.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "local.storage")
@Data
public class LocalStorageProperties {
    private String basePath = "/tmp/documents";
}

