package com.nivasafinance.services.whatsapp.provider.wati;

import org.springframework.web.client.RestTemplate;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;

public class WatiApiClient {
    private final WatiHttpClient httpClient;

    public WatiApiClient() {
        this.httpClient = new WatiHttpClient(new RestTemplate());
    }

    public WatiApiClient(WatiHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public WhatsAppTemplateResponse sendTemplate(WatiConfiguration config, WhatsAppTemplateRequest request) {
        return httpClient.sendTemplate(config, request);
    }

    public WhatsAppTemplateResponse getTemplateStatus(WatiConfiguration config, String phoneNumber) {
        return httpClient.getTemplateStatus(config, phoneNumber);
    }
}

