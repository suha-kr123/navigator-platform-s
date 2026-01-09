package com.nivasafinance.services.voice.provider.exotel.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExotelConfiguration {
    private String accountSid;
    private String baseUrl;
    private String callWebhookUrl;
    private String campaignWebhookUrl;
    private String campaignCallWebhookUrl;
    private String apiKey;
    private String apiToken;
}

