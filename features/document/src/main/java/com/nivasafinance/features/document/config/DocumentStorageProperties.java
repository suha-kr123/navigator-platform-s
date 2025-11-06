package com.nivasafinance.features.document.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "document.storage")
@Data
public class DocumentStorageProperties {
    
    private String provider = "LOCAL";
    private SignedUrlProperties signedUrl = new SignedUrlProperties();
    private FallbackProperties fallback = new FallbackProperties();
    
    @Data
    public static class SignedUrlProperties {
        private Long expirationSeconds = 300L;
    }
    
    @Data
    public static class FallbackProperties {
        private Boolean enabled = true;
    }
}

