package com.nivasafinance.features.document.health;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "document.storage.provider",
        havingValue = "AWS_S3",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class S3HealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(S3HealthIndicator.class);
    
    private final AmazonS3 amazonS3;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    public boolean checkHealth() {
        try {
            amazonS3.getBucketLocation(bucketName);
            logger.info("S3 health check passed: bucket={}, region={}", bucketName, amazonS3.getRegionName());
            return true;
        } catch (Exception e) {
            logger.error("S3 health check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}


