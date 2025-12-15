package com.nivasafinance.common.messaging.config;

import com.nivasafinance.common.messaging.enums.MessageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
    @DependsOn("messagingSecretsLoader")
    public SqsClient sqsClient() {
        // Check if provider is SQS (either from property or auto-detected)
        if (messagingProperties.getProvider() != MessageProvider.SQS) {
            log.debug("SQS provider not enabled, skipping SqsClient creation");
            return null;
        }
        
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

