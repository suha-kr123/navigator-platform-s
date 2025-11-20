package com.nivasafinance.common.messaging.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
@RequiredArgsConstructor
@Slf4j
public class MessagingConfig {

    private final MessagingProperties messagingProperties;
    private final MessagingConfigurationValidator configurationValidator;

    @Bean
    @ConditionalOnProperty(name = "messaging.provider", havingValue = "SQS")
    public SqsClient sqsClient() {
        configurationValidator.validateSqsConfiguration();
        MessagingProperties.SqsProperties sqs = messagingProperties.getSqs();

        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(sqs.getRegion()))
                .credentialsProvider(resolveAwsCredentials(sqs));

        if (StringUtils.hasText(sqs.getEndpoint())) {
            builder.endpointOverride(URI.create(sqs.getEndpoint()));
            log.info("Using custom SQS endpoint {}", sqs.getEndpoint());
        }

        return builder.build();
    }

    private AwsCredentialsProvider resolveAwsCredentials(MessagingProperties.SqsProperties sqs) {
        if (StringUtils.hasText(sqs.getAccessKey()) && StringUtils.hasText(sqs.getSecretKey())) {
            log.info("Using static AWS credentials for SQS");
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(sqs.getAccessKey(), sqs.getSecretKey())
            );
        }
        log.info("Using default AWS credentials provider chain for SQS");
        return DefaultCredentialsProvider.create();
    }
}

