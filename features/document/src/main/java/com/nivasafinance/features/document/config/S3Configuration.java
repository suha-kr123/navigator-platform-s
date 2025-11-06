package com.nivasafinance.features.document.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "document.storage.provider",
        havingValue = "AWS_S3",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class S3Configuration {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Configuration.class);
    
    @Value("${aws.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;
    
    @Value("${aws.s3.region}")
    private String region;
    
    @Value("${aws.s3.use-iam-roles:false}")
    private Boolean useIamRoles;
    
    @Bean
    public AmazonS3 amazonS3() {
        logger.info("Initializing AWS S3 Configuration");
        
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withForceGlobalBucketAccessEnabled(true)
                .withClientConfiguration(clientConfiguration);
        
        if (useIamRoles && (accessKeyId == null || accessKeyId.isBlank() || 
                secretAccessKey == null || secretAccessKey.isBlank())) {
            logger.debug("Using IAM Role-based authentication for region: {}", region);
            builder.withCredentials(
                    WebIdentityTokenCredentialsProvider.builder()
                            .roleSessionName("navigator-platform-session")
                            .build()
            );
        } else {
            logger.debug("Using Access Key-based authentication for region: {}", region);
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
            AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            builder.withCredentials(credentialsProvider);
        }
        
        AmazonS3 s3Client = builder.build();
        logger.info("S3 Client initialized successfully for region: {}", s3Client.getRegionName());
        return s3Client;
    }
}


