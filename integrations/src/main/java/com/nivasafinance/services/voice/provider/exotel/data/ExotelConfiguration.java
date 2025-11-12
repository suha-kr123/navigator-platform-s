package com.nivasafinance.services.voice.provider.exotel.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExotelConfiguration {
    private String accountSid;
    private String authToken;
    private String subdomain;
    private String callerId;
    private String webhookUrl;
    private String apiKey = "";
    private String apiToken = "";
    private boolean recordingEnabled = true;
    private int maxCallDuration = 3600;
    private int retryAttempts = 3;
    private int timeout = 30;
}

