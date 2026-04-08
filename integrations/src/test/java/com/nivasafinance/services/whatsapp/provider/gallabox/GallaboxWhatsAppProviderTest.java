package com.nivasafinance.services.whatsapp.provider.gallabox;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.gallabox.data.GallaboxConfiguration;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GallaboxWhatsAppProviderTest {

    @Mock
    private GallaboxApiClient apiClient;

    private GallaboxWhatsAppProvider provider;

    @BeforeEach
    void setUp() {
        provider = new GallaboxWhatsAppProvider();
        ReflectionTestUtils.setField(provider, "apiClient", apiClient);
    }

    @Test
    void getKey_returnsGallabox() {
        assertEquals(ThirdPartyProviderList.GALLABOX, provider.getKey(), "Key should be GALLABOX");
    }

    @Test
    void setupConfiguration_validMap_returnsConfiguration() {
        Map<String, String> map = new HashMap<>();
        map.put("api_endpoint", "https://custom.endpoint.com");
        map.put("api_key", "test-key");
        map.put("api_secret", "test-secret");
        map.put("channel_id", "ch-001");
        map.put("timeout", "60");
        map.put("retry_attempts", "5");

        GallaboxConfiguration config = provider.setupConfiguration(map);

        assertEquals("https://custom.endpoint.com", config.getApiEndpoint(), "API endpoint should match");
        assertEquals("test-key", config.getApiKey(), "API key should match");
        assertEquals("test-secret", config.getApiSecret(), "API secret should match");
        assertEquals("ch-001", config.getChannelId(), "Channel ID should match");
        assertEquals(60, config.getTimeout(), "Timeout should match");
        assertEquals(5, config.getRetryAttempts(), "Retry attempts should match");
    }

    @Test
    void setupConfiguration_missingEndpoint_usesDefault() {
        Map<String, String> map = new HashMap<>();
        map.put("api_key", "key");
        map.put("api_secret", "secret");
        map.put("channel_id", "ch-001");

        GallaboxConfiguration config = provider.setupConfiguration(map);

        assertEquals("https://server.gallabox.com/devapi/messages/whatsapp", config.getApiEndpoint(),
                "Should use default API endpoint");
    }

    @Test
    void setupConfiguration_missingApiKey_throwsIllegalArgument() {
        Map<String, String> map = new HashMap<>();
        map.put("api_secret", "secret");
        map.put("channel_id", "ch-001");

        assertThrows(IllegalArgumentException.class,
                () -> provider.setupConfiguration(map),
                "Should throw when API key is missing");
    }

    @Test
    void setupConfiguration_emptyApiKey_throwsIllegalArgument() {
        Map<String, String> map = new HashMap<>();
        map.put("api_key", "");
        map.put("api_secret", "secret");
        map.put("channel_id", "ch-001");

        assertThrows(IllegalArgumentException.class,
                () -> provider.setupConfiguration(map),
                "Should throw when API key is empty");
    }

    @Test
    void setupConfiguration_missingApiSecret_throwsIllegalArgument() {
        Map<String, String> map = new HashMap<>();
        map.put("api_key", "key");
        map.put("channel_id", "ch-001");

        assertThrows(IllegalArgumentException.class,
                () -> provider.setupConfiguration(map),
                "Should throw when API secret is missing");
    }

    @Test
    void setupConfiguration_missingChannelId_throwsIllegalArgument() {
        Map<String, String> map = new HashMap<>();
        map.put("api_key", "key");
        map.put("api_secret", "secret");

        assertThrows(IllegalArgumentException.class,
                () -> provider.setupConfiguration(map),
                "Should throw when channel ID is missing");
    }

    @Test
    void setupConfiguration_invalidTimeout_usesDefault() {
        Map<String, String> map = new HashMap<>();
        map.put("api_key", "key");
        map.put("api_secret", "secret");
        map.put("channel_id", "ch-001");
        map.put("timeout", "invalid");

        GallaboxConfiguration config = provider.setupConfiguration(map);

        assertEquals(30, config.getTimeout(), "Should use default timeout on invalid value");
    }

    @Test
    void setupConfiguration_invalidRetryAttempts_usesDefault() {
        Map<String, String> map = new HashMap<>();
        map.put("api_key", "key");
        map.put("api_secret", "secret");
        map.put("channel_id", "ch-001");
        map.put("retry_attempts", "abc");

        GallaboxConfiguration config = provider.setupConfiguration(map);

        assertEquals(3, config.getRetryAttempts(), "Should use default retry attempts on invalid value");
    }

    @Test
    void sendTemplate_validRequest_delegatesToApiClient() {
        Map<String, String> configMap = Map.of(
                "api_key", "key", "api_secret", "secret", "channel_id", "ch-001");
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();
        tpConfig.setConfigurations(configMap);

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("9876543210")
                .templateName("test_template")
                .build();

        WhatsAppTemplateResponse expected = WhatsAppTemplateResponse.builder()
                .messageId("msg-001").status("ACCEPTED").build();
        when(apiClient.sendTemplate(any(GallaboxConfiguration.class), eq(request))).thenReturn(expected);

        WhatsAppTemplateResponse result = provider.sendTemplate(request, tpConfig, new BusinessContext());

        assertEquals("msg-001", result.getMessageId(), "Message ID should match");
        verify(apiClient).sendTemplate(any(GallaboxConfiguration.class), eq(request));
    }

    @Test
    void sendTemplate_nullPhoneNumber_throwsIllegalArgument() {
        Map<String, String> configMap = Map.of(
                "api_key", "key", "api_secret", "secret", "channel_id", "ch-001");
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
    void sendTemplate_blankPhoneNumber_throwsIllegalArgument() {
        Map<String, String> configMap = Map.of(
                "api_key", "key", "api_secret", "secret", "channel_id", "ch-001");
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();
        tpConfig.setConfigurations(configMap);

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("   ")
                .templateName("test_template")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> provider.sendTemplate(request, tpConfig, new BusinessContext()),
                "Should throw when phone number is blank");
    }

    @Test
    void sendTemplate_nullTemplateName_throwsIllegalArgument() {
        Map<String, String> configMap = Map.of(
                "api_key", "key", "api_secret", "secret", "channel_id", "ch-001");
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();
        tpConfig.setConfigurations(configMap);

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("9876543210")
                .templateName(null)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> provider.sendTemplate(request, tpConfig, new BusinessContext()),
                "Should throw when template name is null");
    }

    @Test
    void getTemplateStatus_returnsPlaceholderResponse() {
        ThirdPartyConfig tpConfig = new ThirdPartyConfig();

        WhatsAppTemplateResponse result = provider.getTemplateStatus("9876543210", tpConfig, new BusinessContext());

        assertEquals("9876543210", result.getPhoneNumber(), "Phone number should match");
        assertEquals("unknown", result.getStatus(), "Status should be unknown");
        assertNotNull(result.getErrorMessage(), "Error message should indicate not implemented");
    }
}
