package com.nivasafinance.services.whatsapp.provider.gallabox.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GallaboxConfiguration {
    private String apiEndpoint;
    private String apiKey;
    private String apiSecret;
    private String channelId;
    private int timeout;
    private int retryAttempts;
}
