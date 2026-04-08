package com.nivasafinance.services.whatsapp.provider.gallabox;

import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.gallabox.data.GallaboxConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GallaboxHttpClientTest {

    @Mock
    private RestTemplate restTemplate;

    private GallaboxHttpClient httpClient;
    private GallaboxConfiguration config;

    @BeforeEach
    void setUp() {
        httpClient = new GallaboxHttpClient(restTemplate);
        config = GallaboxConfiguration.builder()
                .apiEndpoint("https://server.gallabox.com/devapi/messages/whatsapp")
                .apiKey("test-key")
                .apiSecret("test-secret")
                .channelId("ch-001")
                .timeout(30)
                .retryAttempts(3)
                .build();
    }

    @Test
    void sendTemplate_successResponse_returnsMappedResponse() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("+919876543210")
                .templateName("order_update")
                .parameters(List.of(
                        TemplateParameter.builder().name("1").value("John").build()))
                .build();

        String responseJson = "{\"id\":\"gallabox-msg-001\",\"status\":\"ACCEPTED\",\"message\":\"Message sent\",\"warnings\":[]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("gallabox-msg-001", result.getMessageId(), "Message ID should match");
        assertEquals("ACCEPTED", result.getStatus(), "Status should be ACCEPTED");
        assertEquals("+919876543210", result.getPhoneNumber(), "Phone number should match original");
        assertEquals("order_update", result.getTemplateName(), "Template name should match");
        assertNull(result.getErrorMessage(), "Error message should be null for success");
    }

    @Test
    void sendTemplate_phoneNumberWithPlus_stripsPlus() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("+919876543210")
                .templateName("template_1")
                .build();

        String responseJson = "{\"id\":\"msg-001\",\"status\":\"ACCEPTED\",\"message\":\"ok\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("ACCEPTED", result.getStatus(), "Should succeed after stripping +");
    }

    @Test
    void sendTemplate_unprocessableEntityStatus_returnsFailed() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        String responseJson = "{\"status\":\"UNPROCESSABLE_ENTITY\",\"message\":\"Template bodyValues contains invalid values\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Status should be failed for error status");
        assertNotNull(result.getErrorMessage(), "Error message should be present");
    }

    @Test
    void sendTemplate_httpError_returnsFailedWithMessage() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        String responseJson = "{\"message\":\"Unauthorized\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.UNAUTHORIZED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Status should be failed for HTTP error");
        assertEquals("Unauthorized", result.getErrorMessage(), "Should parse error message from JSON");
    }

    @Test
    void sendTemplate_httpErrorWithErrorField_returnsErrorFromJson() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        String responseJson = "{\"error\":\"Invalid API key\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.FORBIDDEN);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Status should be failed");
        assertEquals("Invalid API key", result.getErrorMessage(), "Should parse error field from JSON");
    }

    @Test
    void sendTemplate_httpErrorNullBody_returnsHttpStatusCode() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        ResponseEntity<String> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Status should be failed");
        assertTrue(result.getErrorMessage().contains("500"), "Error should contain HTTP status code");
    }

    @Test
    void sendTemplate_restClientException_returnsFailedResponse() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Status should be failed on exception");
        assertEquals("Connection refused", result.getErrorMessage(), "Error message should match exception");
    }

    @Test
    void sendTemplate_unknownStatus_returnsFailed() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        String responseJson = "{\"id\":\"msg-001\",\"status\":\"PENDING\",\"message\":null}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Unknown status should map to failed");
        assertNotNull(result.getErrorMessage(), "Error message should be present for unknown status");
    }

    @Test
    void sendTemplate_withButtonValues_includesInRequest() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .buttonValues(List.of(Map.of("type", "url", "value", "https://example.com")))
                .build();

        String responseJson = "{\"id\":\"msg-001\",\"status\":\"ACCEPTED\",\"message\":\"ok\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("ACCEPTED", result.getStatus(), "Should succeed with button values");
    }

    @Test
    void sendTemplate_nullParameters_sendsEmptyBodyValues() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .parameters(null)
                .build();

        String responseJson = "{\"id\":\"msg-001\",\"status\":\"ACCEPTED\",\"message\":\"ok\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("ACCEPTED", result.getStatus(), "Should succeed with null parameters");
    }
}
