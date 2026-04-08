package com.nivasafinance.integrations.framework.core.data;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationRestRequestTest {

    // ── getUri ──

    @Test
    void getUri_withBaseUrlOnly_returnsEncodedUri() {
        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setUrl("http://api.example.com/v1/resource");

        URI uri = request.getUri();

        assertEquals("http://api.example.com/v1/resource", uri.toString(),
                "URI should match the base URL when no query params are provided");
    }

    @Test
    void getUri_withQueryParams_appendsParamsToUrl() {
        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setUrl("http://api.example.com/v1/resource");

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("key", "value");
        queryParams.add("page", "1");
        request.setQueryParams(queryParams);

        URI uri = request.getUri();

        String uriString = uri.toString();
        assertTrue(uriString.contains("key=value"),
                "URI should contain the 'key' query parameter");
        assertTrue(uriString.contains("page=1"),
                "URI should contain the 'page' query parameter");
    }

    // ── getHttpEntity ──

    @Test
    void getHttpEntity_withBodyAndHeaders_returnsCorrectEntity() {
        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setRequestBody("request-body");

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        request.setHeaders(headers);

        HttpEntity<String> entity = request.getHttpEntity();

        assertEquals("request-body", entity.getBody(),
                "HttpEntity body should match the request body");
        assertEquals("application/json", entity.getHeaders().getFirst("Content-Type"),
                "HttpEntity headers should contain the configured Content-Type");
    }

    @Test
    void getHttpEntity_withNullBody_returnsEntityWithNullBody() {
        IntegrationRestRequest<String> request = new IntegrationRestRequest<>();
        request.setRequestBody(null);
        request.setHeaders(new HttpHeaders());

        HttpEntity<String> entity = request.getHttpEntity();

        assertNull(entity.getBody(),
                "HttpEntity body should be null when request body is null");
    }
}
