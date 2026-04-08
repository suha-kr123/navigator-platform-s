package com.nivasafinance.services.whatsapp.provider.wati;

import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatiHttpClientTest {

    @Mock
    private RestTemplate restTemplate;

    private WatiHttpClient httpClient;
    private WatiConfiguration config;

    @BeforeEach
    void setUp() {
        httpClient = new WatiHttpClient(restTemplate);
        config = WatiConfiguration.builder()
                .apiEndpoint("https://live-server.wati.io")
                .accessToken("test-token")
                .clientId("client-001")
                .timeout(30)
                .retryAttempts(3)
                .build();
    }

    @Test
    void sendTemplate_successResponse_returnsSentStatus() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("+919876543210")
                .templateName("order_update")
                .broadcastName("broadcast_1")
                .parameters(List.of(
                        TemplateParameter.builder().name("1").value("John").build()))
                .build();

        String responseJson = "{\"result\":true,\"id\":\"wati-msg-001\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("wati-msg-001", result.getMessageId(), "Message ID should match");
        assertEquals("sent", result.getStatus(), "Status should be sent for result=true");
        assertEquals("+919876543210", result.getPhoneNumber(), "Phone number should match original");
        assertEquals("order_update", result.getTemplateName(), "Template name should match");
        assertNull(result.getErrorMessage(), "Error message should be null for success");
    }

    @Test
    void sendTemplate_failedResult_returnsFailedStatus() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        String responseJson = "{\"result\":false,\"error\":\"Invalid template\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Status should be failed for result=false");
        assertEquals("Invalid template", result.getErrorMessage(), "Error message should match");
    }

    @Test
    void sendTemplate_nullBroadcastName_usesDefault() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .broadcastName(null)
                .build();

        String responseJson = "{\"result\":true,\"id\":\"msg-001\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("sent", result.getStatus(), "Should succeed with default broadcast name");
    }

    @Test
    void sendTemplate_emptyBroadcastName_usesDefault() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .broadcastName("")
                .build();

        String responseJson = "{\"result\":true,\"id\":\"msg-001\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("sent", result.getStatus(), "Should succeed with default broadcast name");
    }

    @Test
    void sendTemplate_restClientException_returnsFailedResponse() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("failed", result.getStatus(), "Status should be failed on exception");
        assertEquals("Connection timeout", result.getErrorMessage(), "Error message should match exception");
    }

    @Test
    void sendTemplate_nullParameters_sendsEmptyList() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .parameters(null)
                .build();

        String responseJson = "{\"result\":true,\"id\":\"msg-001\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals("sent", result.getStatus(), "Should succeed with null parameters");
    }

    @Test
    void sendTemplate_rawResponseBodyCaptured() {
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber("919876543210")
                .templateName("template_1")
                .build();

        String responseJson = "{\"result\":true,\"id\":\"msg-001\"}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.sendTemplate(config, request);

        assertEquals(responseJson, result.getRawResponseBody(), "Raw response body should be captured");
    }

    @Test
    void getTemplateStatus_successResponse_returnsStatus() {
        String responseJson = "{\"result\":\"success\",\"messages\":{\"items\":[{\"id\":\"msg-001\",\"templateId\":\"tmpl-1\",\"created\":\"2025-01-15T10:30:00\",\"statusString\":\"delivered\"}]}}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.getTemplateStatus(config, "+919876543210");

        assertEquals("+919876543210", result.getPhoneNumber(), "Phone number should match original");
        assertEquals("delivered", result.getStatus(), "Status should be delivered");
        assertEquals("msg-001", result.getMessageId(), "Message ID should match");
        assertEquals("tmpl-1", result.getTemplateName(), "Template name should match");
        assertEquals("2025-01-15T10:30:00", result.getDeliveredAt(), "Delivered at should match");
        assertNull(result.getErrorMessage(), "Error message should be null for success");
    }

    @Test
    void getTemplateStatus_nonSuccessResult_returnsErrorMessage() {
        String responseJson = "{\"result\":\"error\",\"error\":\"Phone not found\",\"messages\":{\"items\":[]}}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.getTemplateStatus(config, "919876543210");

        assertNotNull(result.getErrorMessage(), "Error message should be present");
    }

    @Test
    void getTemplateStatus_emptyMessages_returnsNullMessageId() {
        String responseJson = "{\"result\":\"success\",\"messages\":{\"items\":[]}}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        WhatsAppTemplateResponse result = httpClient.getTemplateStatus(config, "919876543210");

        assertNull(result.getMessageId(), "Message ID should be null when no messages");
    }

    @Test
    void getTemplateStatus_exception_returnsFailedStatus() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        WhatsAppTemplateResponse result = httpClient.getTemplateStatus(config, "919876543210");

        assertEquals("failed", result.getStatus(), "Status should be failed on exception");
        assertEquals("Connection refused", result.getErrorMessage(), "Error message should match");
    }
}
