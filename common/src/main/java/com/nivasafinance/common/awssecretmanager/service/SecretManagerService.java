package com.nivasafinance.common.awssecretmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.awssecretmanager.exception.AwsSecretManagerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecretManagerService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final Region REGION = Region.AP_SOUTH_1;

    private final ObjectMapper objectMapper;

    @Cacheable(value = "secrets", key = "#serviceName")
    public Map<String, Object> getSecret(String serviceName) {
        return getSecretByName(serviceName);
    }

    public Map<String, Object> getSecretByName(String secretName) {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(REGION)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw new AwsSecretManagerException("Failed to retrieve secret '" + secretName + "'", e);
        }

        String secret = getSecretValueResponse.secretString();

        if (secret == null || secret.isBlank()) {
            throw new AwsSecretManagerException("Secret '" + secretName + "' exists but is empty");
        }

        try {
            return objectMapper.readValue(secret, MAP_TYPE);
        } catch (Exception e) {
            throw new AwsSecretManagerException("Failed to parse secret payload for '" + secretName + "'", e);
        }
    }
}

