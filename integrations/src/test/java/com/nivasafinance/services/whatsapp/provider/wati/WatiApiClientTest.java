package com.nivasafinance.services.whatsapp.provider.wati;

import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatiApiClientTest {

    @Mock
    private WatiHttpClient httpClient;

    private WatiApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new WatiApiClient(httpClient);
    }

    @Test
    void sendTemplate_delegatesToHttpClient() {
        WatiConfiguration config = WatiConfiguration.builder()
                .apiEndpoint("https://live-server.wati.io")
                .accessToken("token")
                .build();

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("test_template")
                .build();

        WhatsAppTemplateResponse expected = WhatsAppTemplateResponse.builder()
                .messageId("msg-001")
                .status("sent")
                .build();

        when(httpClient.sendTemplate(config, request)).thenReturn(expected);

        WhatsAppTemplateResponse result = apiClient.sendTemplate(config, request);

        assertSame(expected, result, "Should return response from httpClient");
        verify(httpClient).sendTemplate(config, request);
    }

    @Test
    void getTemplateStatus_delegatesToHttpClient() {
        WatiConfiguration config = WatiConfiguration.builder()
                .apiEndpoint("https://live-server.wati.io")
                .accessToken("token")
                .build();

        WhatsAppTemplateResponse expected = WhatsAppTemplateResponse.builder()
                .phoneNumber("919876543210")
                .status("delivered")
                .build();

        when(httpClient.getTemplateStatus(config, "919876543210")).thenReturn(expected);

        WhatsAppTemplateResponse result = apiClient.getTemplateStatus(config, "919876543210");

        assertSame(expected, result, "Should return response from httpClient");
        verify(httpClient).getTemplateStatus(config, "919876543210");
    }

    @Test
    void defaultConstructor_createsHttpClientInternally() {
        WatiApiClient defaultClient = new WatiApiClient();

        Object internalHttpClient = ReflectionTestUtils.getField(defaultClient, "httpClient");
        assertNotNull(internalHttpClient, "Default constructor should create internal httpClient");
    }
}
