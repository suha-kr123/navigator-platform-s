package com.nivasafinance.services.authentication.provider.supabase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.NavigatorRestService;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponseStatus;
import com.nivasafinance.integrations.framework.core.data.IntegrationRestRequest;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;
import com.nivasafinance.services.authentication.dto.AuthCreateUserRequest;
import com.nivasafinance.services.authentication.dto.AuthSendOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpResponse;
import com.nivasafinance.services.authentication.provider.supabase.data.SupabaseConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupabaseAuthProviderTest {

    @Mock
    private NavigatorRestService restService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecretManagerService secretManagerService;

    @InjectMocks
    private SupabaseAuthProvider supabaseAuthProvider;

    @Captor
    private ArgumentCaptor<IntegrationRestRequest<String>> requestCaptor;

    private ThirdPartyConfig thirdPartyConfig;
    private BusinessContext businessContext;

    @BeforeEach
    void setUp() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("projectRef", "test-project");
        configMap.put("apiKey", "test-api-key");

        thirdPartyConfig = new ThirdPartyConfig();
        thirdPartyConfig.setId(1L);
        thirdPartyConfig.setName("supabase");
        thirdPartyConfig.setConfigurations(configMap);

        businessContext = new BusinessContext();
        businessContext.setEntityName("test");
        businessContext.setEntityId(1L);
    }

    // ========== getKey ==========

    @Test
    void getKey_returnsSupabase() {
        assertEquals(ThirdPartyProviderList.SUPABASE, supabaseAuthProvider.getKey());
    }

    // ========== setupConfiguration ==========

    @Test
    void setupConfiguration_directConfig_returnsConfiguration() {
        Map<String, String> configMap = Map.of("projectRef", "my-project", "apiKey", "my-key");

        SupabaseConfiguration result = supabaseAuthProvider.setupConfiguration(configMap);

        assertEquals("my-project", result.getProjectRef());
        assertEquals("my-key", result.getApiKey());
    }

    @Test
    void setupConfiguration_withSecretKey_fetchesFromSecretManager() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("secret_key", "my-secret");

        Map<String, Object> secretMap = Map.of(
                "SUPABASE_PROJECT_REF", "secret-project",
                "SUPABASE_API_KEY", "secret-key"
        );
        when(secretManagerService.getSecret("my-secret")).thenReturn(secretMap);

        SupabaseConfiguration result = supabaseAuthProvider.setupConfiguration(configMap);

        assertEquals("secret-project", result.getProjectRef());
        assertEquals("secret-key", result.getApiKey());
        verify(secretManagerService).getSecret("my-secret");
    }

    @Test
    void setupConfiguration_nullMap_throwsException() {
        assertThrows(NavigatorIntegrationServerException.class,
                () -> supabaseAuthProvider.setupConfiguration(null));
    }

    @Test
    void setupConfiguration_emptyMap_throwsException() {
        assertThrows(NavigatorIntegrationServerException.class,
                () -> supabaseAuthProvider.setupConfiguration(Map.of()));
    }

    // ========== sendOtp ==========

    @Test
    void sendOtp_success_sendsRequest() throws Exception {
        AuthSendOtpRequest request = new AuthSendOtpRequest();
        request.setPhone("+919876543210");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        assertDoesNotThrow(() -> supabaseAuthProvider.sendOtp(request, thirdPartyConfig, businessContext));

        verify(restService).doRestRequest(requestCaptor.capture());
        assertTrue(requestCaptor.getValue().getUrl().contains("test-project.supabase.co/auth/v1/otp"));
    }

    @Test
    void sendOtp_withUsername_includesMetadata() throws Exception {
        AuthSendOtpRequest request = new AuthSendOtpRequest();
        request.setPhone("+919876543210");
        request.setUsername("testuser");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        assertDoesNotThrow(() -> supabaseAuthProvider.sendOtp(request, thirdPartyConfig, businessContext));
        verify(restService).doRestRequest(any());
    }

    @Test
    void sendOtp_nullPhone_throwsException() {
        AuthSendOtpRequest request = new AuthSendOtpRequest();
        request.setPhone(null);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.sendOtp(request, thirdPartyConfig, businessContext));

        verifyNoInteractions(restService);
    }

    @Test
    void sendOtp_blankPhone_throwsException() {
        AuthSendOtpRequest request = new AuthSendOtpRequest();
        request.setPhone("  ");

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.sendOtp(request, thirdPartyConfig, businessContext));
    }

    @Test
    void sendOtp_apiFailure_throwsException() throws Exception {
        AuthSendOtpRequest request = new AuthSendOtpRequest();
        request.setPhone("+919876543210");

        IntegrationResponse failResponse = IntegrationResponse.serverErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server error");
        failResponse.setErrorMessage("Rate limit exceeded");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(failResponse);

        NavigatorIntegrationClientException exception = assertThrows(
                NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.sendOtp(request, thirdPartyConfig, businessContext));

        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    }

    @Test
    void sendOtp_apiFailureNoErrorMessage_usesResponseBody() throws Exception {
        AuthSendOtpRequest request = new AuthSendOtpRequest();
        request.setPhone("+919876543210");

        IntegrationResponse failResponse = IntegrationResponse.serverErrorResponse(
                HttpStatus.BAD_REQUEST, "Bad request body");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(failResponse);

        NavigatorIntegrationClientException exception = assertThrows(
                NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.sendOtp(request, thirdPartyConfig, businessContext));

        assertTrue(exception.getMessage().contains("Bad request body"));
    }

    @Test
    void sendOtp_serializationFails_throwsException() throws Exception {
        AuthSendOtpRequest request = new AuthSendOtpRequest();
        request.setPhone("+919876543210");

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.sendOtp(request, thirdPartyConfig, businessContext));
    }

    // ========== verifyOtp ==========

    @Test
    void verifyOtp_success_returnsTokens() throws Exception {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone("+919876543210");
        request.setToken("123456");

        String responseJson = "{\"access_token\":\"abc\",\"refresh_token\":\"def\",\"expires_at\":1234567890}";
        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, responseJson);

        com.nivasafinance.services.authentication.provider.supabase.data.SupabaseVerifyOtpResponse supabaseResp =
                new com.nivasafinance.services.authentication.provider.supabase.data.SupabaseVerifyOtpResponse();
        supabaseResp.setAccessToken("abc");
        supabaseResp.setRefreshToken("def");
        supabaseResp.setExpiresAt(1234567890L);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);
        when(objectMapper.readValue(eq(responseJson),
                eq(com.nivasafinance.services.authentication.provider.supabase.data.SupabaseVerifyOtpResponse.class)))
                .thenReturn(supabaseResp);

        AuthVerifyOtpResponse result = supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext);

        assertNotNull(result);
        assertEquals("abc", result.getAccessToken());
        assertEquals("def", result.getRefreshToken());
        assertEquals(1234567890L, result.getExpiresAt());
    }

    @Test
    void verifyOtp_nullPhone_throwsException() {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone(null);
        request.setToken("123456");

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext));
    }

    @Test
    void verifyOtp_nullToken_throwsException() {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone("+919876543210");
        request.setToken(null);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext));
    }

    @Test
    void verifyOtp_blankToken_throwsException() {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone("+919876543210");
        request.setToken("  ");

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext));
    }

    @Test
    void verifyOtp_apiFailure_throwsException() throws Exception {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone("+919876543210");
        request.setToken("123456");

        IntegrationResponse failResponse = IntegrationResponse.serverErrorResponse(
                HttpStatus.UNAUTHORIZED, "Invalid token");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(failResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext));
    }

    @Test
    void verifyOtp_emptyResponseBody_throwsException() throws Exception {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone("+919876543210");
        request.setToken("123456");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext));
    }

    @Test
    void verifyOtp_nullResponseBody_throwsException() throws Exception {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone("+919876543210");
        request.setToken("123456");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext));
    }

    @Test
    void verifyOtp_malformedResponseJson_throwsException() throws Exception {
        AuthVerifyOtpRequest request = new AuthVerifyOtpRequest();
        request.setPhone("+919876543210");
        request.setToken("123456");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "not-json");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);
        when(objectMapper.readValue(eq("not-json"), any(Class.class)))
                .thenThrow(new JsonProcessingException("Parse error") {});

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.verifyOtp(request, thirdPartyConfig, businessContext));
    }

    // ========== createUser ==========

    @Test
    void createUser_success_sendsAdminRequest() throws Exception {
        AuthCreateUserRequest request = new AuthCreateUserRequest();
        request.setPhone("+919876543210");
        request.setPassword("securePass");
        request.setEmail("test@example.com");
        request.setUserMetadata(Map.of("role", "admin"));

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        assertDoesNotThrow(
                () -> supabaseAuthProvider.createUser(request, thirdPartyConfig, businessContext));

        verify(restService).doRestRequest(requestCaptor.capture());
        assertTrue(requestCaptor.getValue().getUrl().contains("test-project.supabase.co/auth/v1/admin/users"));
        assertTrue(requestCaptor.getValue().getHeaders().containsKey("Authorization"));
    }

    @Test
    void createUser_nullEmail_setsEmailToNull() throws Exception {
        AuthCreateUserRequest request = new AuthCreateUserRequest();
        request.setPhone("+919876543210");
        request.setPassword("pass");
        request.setEmail(null);

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        assertDoesNotThrow(
                () -> supabaseAuthProvider.createUser(request, thirdPartyConfig, businessContext));
    }

    @Test
    void createUser_blankEmail_setsEmailToNull() throws Exception {
        AuthCreateUserRequest request = new AuthCreateUserRequest();
        request.setPhone("+919876543210");
        request.setPassword("pass");
        request.setEmail("  ");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        assertDoesNotThrow(
                () -> supabaseAuthProvider.createUser(request, thirdPartyConfig, businessContext));
    }

    @Test
    void createUser_apiFailure_throwsException() throws Exception {
        AuthCreateUserRequest request = new AuthCreateUserRequest();
        request.setPhone("+919876543210");
        request.setPassword("pass");

        IntegrationResponse failResponse = IntegrationResponse.serverErrorResponse(
                HttpStatus.CONFLICT, "User already exists");
        failResponse.setErrorMessage("Duplicate phone");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(restService.doRestRequest(any())).thenReturn(failResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> supabaseAuthProvider.createUser(request, thirdPartyConfig, businessContext));
    }
}
