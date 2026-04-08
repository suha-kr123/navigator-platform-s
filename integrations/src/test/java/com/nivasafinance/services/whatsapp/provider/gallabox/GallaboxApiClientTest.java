package com.nivasafinance.services.whatsapp.provider.gallabox;

import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.gallabox.data.GallaboxConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GallaboxApiClientTest {

    @Mock
    private GallaboxHttpClient httpClient;

    private GallaboxApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new GallaboxApiClient(httpClient);
    }

    @Test
    void sendTemplate_delegatesToHttpClient() {
        GallaboxConfiguration config = GallaboxConfiguration.builder()
                .apiEndpoint("https://server.gallabox.com")
                .apiKey("key")
                .apiSecret("secret")
                .channelId("ch-001")
                .build();

        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("test_template")
                .build();

        WhatsAppTemplateResponse expected = WhatsAppTemplateResponse.builder()
                .messageId("msg-001")
                .status("ACCEPTED")
                .build();

        when(httpClient.sendTemplate(config, request)).thenReturn(expected);

        WhatsAppTemplateResponse result = apiClient.sendTemplate(config, request);

        assertSame(expected, result, "Should return response from httpClient");
        verify(httpClient).sendTemplate(config, request);
    }

    @Test
    void defaultConstructor_createsHttpClientInternally() {
        GallaboxApiClient defaultClient = new GallaboxApiClient();

        Object internalHttpClient = ReflectionTestUtils.getField(defaultClient, "httpClient");
        assertNotNull(internalHttpClient, "Default constructor should create internal httpClient");
    }
}
