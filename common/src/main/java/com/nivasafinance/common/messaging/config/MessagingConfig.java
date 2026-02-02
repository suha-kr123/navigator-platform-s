package com.nivasafinance.common.messaging.config;

import com.nivasafinance.common.messaging.enums.MessageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.backoff.EqualJitterBackoffStrategy;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;
import java.time.Duration;

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
        // Check provider at runtime (after secrets are loaded) instead of using @ConditionalOnProperty
        if (messagingProperties.getProvider() != MessageProvider.SQS) {
            log.debug("SQS provider not configured, skipping SqsClient bean creation. Current provider: {}", 
                    messagingProperties.getProvider());
            return null;
        }
        
        try {
        configurationValidator.validateSqsConfiguration();
        MessagingProperties.SqsProperties sqs = messagingProperties.getSqs();

            // Configure HTTP client with longer timeouts for local/network latency
            software.amazon.awssdk.http.SdkHttpClient httpClient = ApacheHttpClient.builder()
                    .connectionTimeout(Duration.ofSeconds(10))
                    .socketTimeout(Duration.ofSeconds(30))
                    .build();

            // Configure retry policy for network issues
            BackoffStrategy backoffStrategy = EqualJitterBackoffStrategy.builder()
                    .baseDelay(Duration.ofMillis(100))
                    .maxBackoffTime(Duration.ofSeconds(2))
                    .build();
            
            RetryPolicy retryPolicy = RetryPolicy.builder()
                    .numRetries(3)
                    .backoffStrategy(backoffStrategy)
                    .build();

            ClientOverrideConfiguration clientConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofSeconds(60))
                    .apiCallAttemptTimeout(Duration.ofSeconds(30))
                    .retryPolicy(retryPolicy)
                    .build();

        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(sqs.getRegion()))
                    .credentialsProvider(resolveAwsCredentials(sqs))
                    .httpClient(httpClient)
                    .overrideConfiguration(clientConfig);

        if (StringUtils.hasText(sqs.getEndpoint())) {
            builder.endpointOverride(URI.create(sqs.getEndpoint()));
            log.info("Using custom SQS endpoint {}", sqs.getEndpoint());
        }

            SqsClient client = builder.build();
            log.info("Successfully created SqsClient for region: {} with extended timeouts (connection: 10s, read/write: 30s, API: 60s)", 
                    sqs.getRegion());
            return client;
        } catch (Exception ex) {
            log.error("Failed to create SqsClient bean. SQS functionality will not be available. " +
                    "Provider is set to SQS but configuration is invalid. Error: {}", ex.getMessage(), ex);
            // Return null instead of throwing - this allows the application to start
            // MessagePublisherFactory will handle the fallback to LOCAL
            return null;
        }
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

