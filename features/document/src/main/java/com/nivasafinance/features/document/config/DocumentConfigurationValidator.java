package com.nivasafinance.features.document.config;

import com.nivasafinance.features.document.health.LocalStorageHealthIndicator;
import com.nivasafinance.features.document.health.S3HealthIndicator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentConfigurationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentConfigurationValidator.class);
    
    @Value("${document.storage.provider:LOCAL}")
    private String storageProvider;
    
    @Value("${aws.s3.bucket-name:}")
    private String s3BucketName;
    
    @Value("${aws.s3.region:}")
    private String s3Region;
    
    @Value("${local.storage.base-path:/tmp/documents}")
    private String localBasePath;
    
    @Autowired(required = false)
    private S3HealthIndicator s3HealthIndicator;
    
    @Autowired(required = false)
    private LocalStorageHealthIndicator localStorageHealthIndicator;
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        logger.info("Validating document storage configuration...");
        
        try {
            String upperProvider = storageProvider.toUpperCase();
            switch (upperProvider) {
                case "AWS_S3":
                    validateS3Configuration();
                    break;
                case "LOCAL":
                    validateLocalConfiguration();
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid storage provider: " + storageProvider + ". Valid options: AWS_S3, LOCAL"
                    );
            }
            
            logger.info("Document storage configuration validation completed successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Invalid storage provider: {}. Valid options: AWS_S3, LOCAL", storageProvider);
            throw new RuntimeException("Invalid document storage configuration: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Document storage configuration validation failed", e);
            throw new RuntimeException("Document storage configuration validation failed: " + e.getMessage(), e);
        }
    }
    
    private void validateS3Configuration() {
        logger.info("Validating AWS S3 configuration...");
        
        if (s3BucketName == null || s3BucketName.isBlank()) {
            throw new IllegalArgumentException("AWS S3 bucket name is required when using AWS_S3 provider");
        }
        
        if (s3Region == null || s3Region.isBlank()) {
            throw new IllegalArgumentException("AWS S3 region is required when using AWS_S3 provider");
        }
        
        if (s3HealthIndicator != null) {
            if (!s3HealthIndicator.checkHealth()) {
                logger.warn("S3 health check failed, but continuing with configuration validation");
            }
        }
        
        logger.info("AWS S3 configuration validated: bucket={}, region={}", s3BucketName, s3Region);
    }
    
    private void validateLocalConfiguration() {
        logger.info("Validating local storage configuration...");
        
        if (localBasePath == null || localBasePath.isBlank()) {
            throw new IllegalArgumentException("Local storage base path cannot be blank");
        }
        
        if (localStorageHealthIndicator != null) {
            if (!localStorageHealthIndicator.checkHealth()) {
                logger.warn("Local storage health check failed, but continuing with configuration validation");
            }
        }
        
        logger.info("Local storage configuration validated: basePath={}", localBasePath);
    }
}


