package com.nivasafinance.services.whatsapp.provider.aswsecretmanagerconfig;

import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile("!dev")
public class WatiConfig {

    private final SecretManagerService secretManagerService;
    private volatile Map<String, Object> watiSecrets;

    /**
     * Loads secrets lazily on first use instead of at startup.
     * This prevents application startup failure if secrets are temporarily unavailable.
     */
    private Map<String, Object> getWatiSecrets() {
        if (watiSecrets == null) {
            synchronized (this) {
                if (watiSecrets == null) {
                    watiSecrets = secretManagerService.getSecret("CUSTOMER_WATI");
                }
            }
        }
        return watiSecrets;
    }

    public String getApiKey() {
        return getWatiSecrets().get("api_key").toString();
    }

    public String getBaseUrl() {
        return getWatiSecrets().get("base_url").toString();
    }

    public String getSenderId() {
        return getWatiSecrets().get("sender_id").toString();
    }
}

