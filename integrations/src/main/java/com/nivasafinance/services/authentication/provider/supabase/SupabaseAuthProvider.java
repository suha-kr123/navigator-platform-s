package com.nivasafinance.services.authentication.provider.supabase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.NavigatorRestService;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationRestRequest;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;
import com.nivasafinance.services.authentication.dto.AuthCreateUserRequest;
import com.nivasafinance.services.authentication.dto.AuthSendOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpResponse;
import com.nivasafinance.services.authentication.provider.AuthenticationProvider;
import com.nivasafinance.services.authentication.provider.supabase.data.SupabaseAdminCreateUserRequest;
import com.nivasafinance.services.authentication.provider.supabase.data.SupabaseConfiguration;
import com.nivasafinance.services.authentication.provider.supabase.data.SupabaseSendOtpRequest;
import com.nivasafinance.services.authentication.provider.supabase.data.SupabaseVerifyOtpRequest;
import com.nivasafinance.services.authentication.provider.supabase.data.SupabaseVerifyOtpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SupabaseAuthProvider implements AuthenticationProvider {

    private static final String OTP_PATH = "/auth/v1/otp";
    private static final String VERIFY_PATH = "/auth/v1/verify";
    private static final String ADMIN_USERS_PATH = "/auth/v1/admin/users";

    private final NavigatorRestService restService;
    private final ObjectMapper objectMapper;
    private final SecretManagerService secretManagerService;

    @Override
    public ThirdPartyProviderList getKey() {
        return ThirdPartyProviderList.SUPABASE;
    }

    @Override
    public SupabaseConfiguration setupConfiguration(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            throw new NavigatorIntegrationServerException("Supabase configuration is empty.");
        }

        if(map.containsKey("secret_key")) {
            Map<String,Object> secretMap = secretManagerService.getSecret(map.get("secret_key"));
            return new SupabaseConfiguration(
                    secretMap.get("SUPABASE_PROJECT_REF").toString(),
                    secretMap.get("SUPABASE_API_KEY").toString()
            );
        }

        return new SupabaseConfiguration(
                map.get("projectRef"),
                map.get("apiKey")
        );
    }

    @Override
    public void sendOtp(
            AuthSendOtpRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        SupabaseConfiguration supabaseConfig = setupConfiguration(config.getConfigurations());
        validateSendOtpRequest(request);
        SupabaseSendOtpRequest.SupabaseSendOtpRequestBuilder bodyBuilder = SupabaseSendOtpRequest.builder()
                .phone(request.getPhone())
                .options(new SupabaseSendOtpRequest.Options("sms"));
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            bodyBuilder.data(new SupabaseSendOtpRequest.MetaData(request.getUsername()));
        }
        SupabaseSendOtpRequest body = bodyBuilder.build();
        IntegrationRestRequest<String> restRequest = buildSendOtpRequest(body, supabaseConfig, config, businessContext);
        IntegrationResponse response = restService.doRestRequest(restRequest);
        if (!response.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Supabase send OTP failed: "
                            + (response.getErrorMessage() != null
                            ? response.getErrorMessage()
                            : response.getResponseBody()));
        }
    }

    @Override
    public AuthVerifyOtpResponse verifyOtp(
            AuthVerifyOtpRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        SupabaseConfiguration supabaseConfig = setupConfiguration(config.getConfigurations());
        validateVerifyOtpRequest(request);
        SupabaseVerifyOtpRequest body = new SupabaseVerifyOtpRequest(request.getPhone(), "sms", request.getToken());
        IntegrationRestRequest<String> restRequest = buildVerifyOtpRequest(body, supabaseConfig, config, businessContext);
        IntegrationResponse response = restService.doRestRequest(restRequest);
        if (!response.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Supabase verify OTP failed: "
                            + (response.getErrorMessage() != null
                            ? response.getErrorMessage()
                            : response.getResponseBody()));
        }
        return mapVerifyResponse(response);
    }

    @Override
    public void createUser(
            AuthCreateUserRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        SupabaseConfiguration supabaseConfig = setupConfiguration(config.getConfigurations());
        SupabaseAdminCreateUserRequest body = SupabaseAdminCreateUserRequest.builder()
                .phone(request.getPhone())
                .password(request.getPassword())
                .email(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail() : null)
                .phoneConfirm(true)
                .userMetadata(request.getUserMetadata())
                .build();
        IntegrationRestRequest<String> restRequest = buildAdminCreateUserRequest(body, supabaseConfig, config, businessContext);
        IntegrationResponse response = restService.doRestRequest(restRequest);
        if (!response.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Supabase admin create user failed: "
                            + (response.getErrorMessage() != null ? response.getErrorMessage() : response.getResponseBody()));
        }
    }

    private void validateSendOtpRequest(AuthSendOtpRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new NavigatorIntegrationClientException("Phone cannot be empty");
        }
    }

    private void validateVerifyOtpRequest(AuthVerifyOtpRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new NavigatorIntegrationClientException("Phone cannot be empty");
        }
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new NavigatorIntegrationClientException("Token cannot be empty");
        }
    }

    private IntegrationRestRequest<String> buildSendOtpRequest(
            SupabaseSendOtpRequest body,
            SupabaseConfiguration config,
            ThirdPartyConfig thirdPartyConfig,
            BusinessContext businessContext) {
        String url = "https://" + config.getProjectRef() + ".supabase.co" + OTP_PATH;
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to serialize send OTP request: " + e.getMessage());
        }
        return buildPostRequest(url, requestBody, config, thirdPartyConfig, businessContext);
    }

    private IntegrationRestRequest<String> buildVerifyOtpRequest(
            SupabaseVerifyOtpRequest body,
            SupabaseConfiguration config,
            ThirdPartyConfig thirdPartyConfig,
            BusinessContext businessContext) {
        String url = "https://" + config.getProjectRef() + ".supabase.co" + VERIFY_PATH;
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to serialize verify OTP request: " + e.getMessage());
        }
        return buildPostRequest(url, requestBody, config, thirdPartyConfig, businessContext);
    }

    private IntegrationRestRequest<String> buildAdminCreateUserRequest(
            SupabaseAdminCreateUserRequest body,
            SupabaseConfiguration config,
            ThirdPartyConfig thirdPartyConfig,
            BusinessContext businessContext) {
        String url = "https://" + config.getProjectRef() + ".supabase.co" + ADMIN_USERS_PATH;
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to serialize admin create user request: " + e.getMessage());
        }
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add("apikey", config.getApiKey());
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey());
        ApiContext apiContext = new ApiContext(getKey().getProvideName(), thirdPartyConfig.getId());
        IntegrationRestRequest<String> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.POST);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(requestBody);
        restRequest.setHeaders(headers);
        return restRequest;
    }

    private IntegrationRestRequest<String> buildPostRequest(
            String url,
            String requestBody,
            SupabaseConfiguration config,
            ThirdPartyConfig thirdPartyConfig,
            BusinessContext businessContext) {
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add("apikey", config.getApiKey());

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                thirdPartyConfig.getId()
        );

        IntegrationRestRequest<String> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.POST);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(requestBody);
        restRequest.setHeaders(headers);
        return restRequest;
    }

    private AuthVerifyOtpResponse mapVerifyResponse(IntegrationResponse response) {
        String responseBody = response.getResponseBody();
        if (responseBody == null || responseBody.isBlank()) {
            throw new NavigatorIntegrationClientException("Empty response from Supabase verify");
        }
        try {
            SupabaseVerifyOtpResponse supabaseResponse = objectMapper.readValue(responseBody, SupabaseVerifyOtpResponse.class);
            return AuthVerifyOtpResponse.builder()
                    .accessToken(supabaseResponse.getAccessToken())
                    .refreshToken(supabaseResponse.getRefreshToken())
                    .expiresAt(supabaseResponse.getExpiresAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to parse verify OTP response: " + e.getMessage());
        }
    }
}
