package com.nivasafinance.features.document.health;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ConditionalOnProperty(
        name = "document.storage.provider",
        havingValue = "LOCAL",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class LocalStorageHealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalStorageHealthIndicator.class);
    
    @Value("${local.storage.base-path:/tmp/documents}")
    private String basePath;
    
    public boolean checkHealth() {
        try {
            File baseDir = new File(basePath);
            boolean exists = baseDir.exists();
            boolean writable = baseDir.canWrite();
            boolean readable = baseDir.canRead();
            
            if (exists && writable && readable) {
                logger.info(
                        "Local storage health check passed: basePath={}, writable={}, readable={}",
                        basePath, writable, readable
                );
                return true;
            } else {
                logger.error(
                        "Local storage health check failed: basePath={}, exists={}, writable={}, readable={}",
                        basePath, exists, writable, readable
                );
                return false;
            }
        } catch (Exception e) {
            logger.error("Local storage health check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}


