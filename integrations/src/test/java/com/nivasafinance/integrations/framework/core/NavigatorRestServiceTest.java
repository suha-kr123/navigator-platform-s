package com.nivasafinance.integrations.framework.core;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponseStatus;
import com.nivasafinance.integrations.framework.core.data.IntegrationRestRequest;
import com.nivasafinance.integrations.framework.core.logger.ThirdPartyRequestResponseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NavigatorRestServiceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ThirdPartyRequestResponseLogger thirdPartyRequestResponseLogger;

    private NavigatorRestService service;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        service = new NavigatorRestService(restTemplateBuilder, thirdPartyRequestResponseLogger);
    }

    // ── successful request ──

    @Test
    void doRestRequest_successfulStringResponse_returnsSuccessResponse() {
        IntegrationRestRequest<String> request = buildRequest(false);
        when(thirdPartyRequestResponseLogger.registerRequest(any(), any(), any(), anyString(), any()))
                .thenReturn(1L);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("response-body", HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        IntegrationResponse result = service.doRestRequest(request);

        assertEquals(IntegrationResponseStatus.SUCCESS, result.getResponseStatus(),
                "Response status should be SUCCESS for a 2xx response");
        assertEquals(HttpStatus.OK, result.getHttpStatus(),
                "HTTP status should be OK");
        assertEquals("response-body", result.getResponseBody(),
                "Response body should match the REST response");
    }

    @Test
    void doRestRequest_base64Response_returnsBase64EncodedBody() {
        IntegrationRestRequest<String> request = buildRequest(true);
        when(thirdPartyRequestResponseLogger.registerRequest(any(), any(), any(), anyString(), any()))
                .thenReturn(1L);

        byte[] binaryData = {1, 2, 3, 4};
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(binaryData, HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(responseEntity);

        IntegrationResponse result = service.doRestRequest(request);

        assertEquals(IntegrationResponseStatus.SUCCESS, result.getResponseStatus(),
                "Response status should be SUCCESS for base64 response");
        assertNotNull(result.getResponseBody(),
                "Response body should contain the base64 encoded string");
        assertEquals("AQIDBA==", result.getResponseBody(),
                "Response body should be the base64 encoding of the byte array");
    }

    // ── HTTP error handling ──

    @Test
    void doRestRequest_httpClientError4xx_returnsClientErrorResponse() {
        IntegrationRestRequest<String> request = buildRequest(false);
        when(thirdPartyRequestResponseLogger.registerRequest(any(), any(), any(), anyString(), any()))
                .thenReturn(1L);

        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST, "Bad Request",
                "validation error".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        IntegrationResponse result = service.doRestRequest(request);

        assertEquals(IntegrationResponseStatus.CLIENT_ERROR, result.getResponseStatus(),
                "4xx errors should result in CLIENT_ERROR status");
        assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus(),
                "HTTP status should reflect the 4xx error code");
    }

    @Test
    void doRestRequest_httpServerError5xx_returnsServerErrorResponse() {
        IntegrationRestRequest<String> request = buildRequest(false);
        when(thirdPartyRequestResponseLogger.registerRequest(any(), any(), any(), anyString(), any()))
                .thenReturn(1L);

        HttpServerErrorException exception = new HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Server Error",
                "internal error".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        IntegrationResponse result = service.doRestRequest(request);

        assertEquals(IntegrationResponseStatus.SERVER_ERROR, result.getResponseStatus(),
                "5xx errors should result in SERVER_ERROR status");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus(),
                "HTTP status should reflect the 5xx error code");
    }

    @Test
    void doRestRequest_restClientException_returnsClientErrorWithMessage() {
        IntegrationRestRequest<String> request = buildRequest(false);
        when(thirdPartyRequestResponseLogger.registerRequest(any(), any(), any(), anyString(), any()))
                .thenReturn(1L);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        IntegrationResponse result = service.doRestRequest(request);

        assertEquals(IntegrationResponseStatus.CLIENT_ERROR, result.getResponseStatus(),
                "RestClientException should result in CLIENT_ERROR status");
        assertEquals("Connection refused", result.getErrorMessage(),
                "Error message should contain the RestClientException message");
    }

    // ── response logging ──

    @Test
    void doRestRequest_loggableRequest_registersResponse() {
        IntegrationRestRequest<String> request = buildRequest(false);
        request.setResponseLoggable(true);
        when(thirdPartyRequestResponseLogger.registerRequest(any(), any(), any(), anyString(), any()))
                .thenReturn(1L);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("body", HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        service.doRestRequest(request);

        verify(thirdPartyRequestResponseLogger).registerResponse(eq(1L), eq("body"), anyLong(), eq(200));
    }

    @Test
    void doRestRequest_nonLoggableRequest_doesNotRegisterResponse() {
        IntegrationRestRequest<String> request = buildRequest(false);
        request.setResponseLoggable(false);
        when(thirdPartyRequestResponseLogger.registerRequest(any(), any(), any(), anyString(), any()))
                .thenReturn(1L);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("body", HttpStatus.OK);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        service.doRestRequest(request);

        verify(thirdPartyRequestResponseLogger, never()).registerResponse(anyLong(), anyString(), anyLong(), anyInt());
    }

    // ── helper ──

    private IntegrationRestRequest<String> buildRequest(boolean convertToBase64) {
        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setUrl("http://api.example.com/v1/resource");
        request.setMethod(HttpMethod.GET);
        request.setBusinessContext(new BusinessContext("LOAN", 1L, "TEST"));
        request.setApiContext(new ApiContext("provider", 1L));
        request.setRequestBody("request-body");
        request.setHeaders(new HttpHeaders());
        request.setResponseLoggable(true);
        request.setConvertToBase64(convertToBase64);
        return request;
    }
}
