package com.nivasafinance.services.whatsapp.provider.gallabox;

import org.springframework.web.client.RestTemplate;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.gallabox.data.GallaboxConfiguration;

public class GallaboxApiClient {
    private final GallaboxHttpClient httpClient;

    public GallaboxApiClient() {
        this.httpClient = new GallaboxHttpClient(new RestTemplate());
    }

    public GallaboxApiClient(GallaboxHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public WhatsAppTemplateResponse sendTemplate(GallaboxConfiguration config, WhatsAppTemplateRequest request) {
        return httpClient.sendTemplate(config, request);
    }
}
