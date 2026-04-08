package com.nivasafinance.services.whatsapp.provider.wati;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatiWhatsAppProviderTest {

    @Mock
    private WatiApiClient apiClient;

    private WatiWhatsAppProvider provider;

    @BeforeEach
    void setUp() {
        provider = new WatiWhatsAppProvider();
        ReflectionTestUtils.setField(provider, "apiClient", apiClient);
    }

    @Test
    void getKey_returnsWati() {
        assertEquals(ThirdPartyProviderList.WATI, provider.getKey(), "Key should be WATI");
    }

    @Test
    void setupConfiguration_validMap_returnsConfiguration() {
        Map<String, String> map = new HashMap<>();
        map.put("api_endpoint", "https://custom.wati.io");
        map.put("access_token", "token-123");
        map.put("client_id", "client-001");
        map.put("timeout", "45");
        map.put("retry_attempts", "2");

        WatiConfiguration config = provider.setupConfiguration(map);

        assertEquals("https://custom.wati.io", config.getApiEndpoint(), "API endpoint should match");
        assertEquals("token-123", config.getAccessToken(), "Access token should match");
        assertEquals("client-001", config.getClientId(), "Client ID should match");
        assertEquals(45, config.getTimeout(), "Timeout should match");
        assertEquals(2, config.getRetryAttempts(), "Retry attempts should match");
    }

    @Test
    void setupConfiguration_missingEndpoint_usesDefault() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "token-123");

        WatiConfiguration config = provider.setupConfiguration(map);

        assertEquals("https://live-server.wati.io", config.getApiEndpoint(),
                "Should use default API endpoint");
    }

    @Test
    void setupConfiguration_missingAccessToken_throwsIllegalArgument() {
        Map<String, String> map = new HashMap<>();
        map.put("api_endpoint", "https://custom.wati.io");

        assertThrows(IllegalArgumentException.class,
                () -> provider.setupConfiguration(map),
                "Should throw when access token is missing");
    }

    @Test
    void setupConfiguration_emptyAccessToken_throwsIllegalArgument() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "");

        assertThrows(IllegalArgumentException.class,
                () -> provider.setupConfiguration(map),
                "Should throw when access token is empty");
    }

    @Test
    void setupConfiguration_invalidTimeout_usesDefault() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "token");
        map.put("timeout", "bad");

        WatiConfiguration config = provider.setupConfiguration(map);

        assertEquals(30, config.getTimeout(), "Should use default timeout on invalid value");
    }

    @Test
    void setupConfiguration_invalidRetryAttempts_usesDefault() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "token");
        map.put("retry_attempts", "xyz");

        WatiConfiguration config = provider.setupConfiguration(map);

        assertEquals(3, config.getRetryAttempts(), "Should use default retry attempts on invalid value");
    }

    @Test
    void setupConfiguration_clientIdKey_usesClientId() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "token");
        map.put("client_id", "client-A");

        WatiConfiguration config = provider.setupConfiguration(map);

        assertEquals("client-A", config.getClientId(), "Should read client_id key");
    }

    @Test
    void setupConfiguration_camelCaseClientId_usesClientId() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "token");
        map.put("clientId", "client-B");

        WatiConfiguration config = provider.setupConfiguration(map);

        assertEquals("client-B", config.getClientId(), "Should read camelCase clientId key");
    }

    @Test
    void setupConfiguration_noClientId_setsNull() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", "token");

        WatiConfiguration config = provider.setupConfiguration(map);

        assertNull(config.getClientId(), "Client ID should be null when not provided");
    }

    @Test
    void sendTemplate_validRequest_delegatesToApiClient() {
        Map<String, String> configMap = Map.of("access_token", "token-123");
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();
        tpConfig.setConfigurations(configMap);

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("9876543210")
                .templateName("test_template")
                .build();

        WhatsAppTemplateResponse expected = WhatsAppTemplateResponse.builder()
                .messageId("msg-001").status("sent").build();
        when(apiClient.sendTemplate(any(WatiConfiguration.class), eq(request))).thenReturn(expected);

        WhatsAppTemplateResponse result = provider.sendTemplate(request, tpConfig, new BusinessContext());

        assertEquals("msg-001", result.getMessageId(), "Message ID should match");
        verify(apiClient).sendTemplate(any(WatiConfiguration.class), eq(request));
    }

    @Test
    void sendTemplate_nullPhoneNumber_throwsIllegalArgument() {
        Map<String, String> configMap = Map.of("access_token", "token-123");
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();
        tpConfig.setConfigurations(configMap);

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber(null)
                .templateName("test_template")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> provider.sendTemplate(request, tpConfig, new BusinessContext()),
                "Should throw when phone number is null");
    }

    @Test
    void sendTemplate_blankTemplateName_throwsIllegalArgument() {
        Map<String, String> configMap = Map.of("access_token", "token-123");
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();
        tpConfig.setConfigurations(configMap);

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("9876543210")
                .templateName("  ")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> provider.sendTemplate(request, tpConfig, new BusinessContext()),
                "Should throw when template name is blank");
    }

    @Test
    void getTemplateStatus_delegatesToApiClient() {
        Map<String, String> configMap = Map.of("access_token", "token-123");
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();
        tpConfig.setConfigurations(configMap);

        WhatsAppTemplateResponse expected = WhatsAppTemplateResponse.builder()
                .phoneNumber("9876543210").status("delivered").build();
        when(apiClient.getTemplateStatus(any(WatiConfiguration.class), eq("9876543210"))).thenReturn(expected);

        WhatsAppTemplateResponse result = provider.getTemplateStatus("9876543210", tpConfig, new BusinessContext());

        assertEquals("delivered", result.getStatus(), "Status should match");
        verify(apiClient).getTemplateStatus(any(WatiConfiguration.class), eq("9876543210"));
    }
}
