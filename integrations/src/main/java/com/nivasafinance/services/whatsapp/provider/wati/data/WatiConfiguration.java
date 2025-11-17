package com.nivasafinance.services.whatsapp.provider.wati.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatiConfiguration {
    private String apiEndpoint;
    private String accessToken;
    private String clientId;
    private int timeout;
    private int retryAttempts;
}

