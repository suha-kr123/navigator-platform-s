package com.nivasafinance.services.whatsapp.provider.aswsecretmanagerconfig;

import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile("!dev")
public class GallaboxConfig {

    private final SecretManagerService secretManagerService;
    private volatile Map<String, Object> customerGallaboxSecrets;
    private volatile Map<String, Object> advisorGallaboxSecrets;
    private final Object lock = new Object();

    /**
     * Loads secrets lazily on first use instead of at startup.
     * This prevents application startup failure if secrets are temporarily unavailable.
     */
    private Map<String, Object> getSecretsForRecipient(String recipientType) {
        boolean isCustomer = "LEAD".equalsIgnoreCase(recipientType);
        
        if (isCustomer) {
            if (customerGallaboxSecrets == null) {
                synchronized (lock) {
                    if (customerGallaboxSecrets == null) {
                        customerGallaboxSecrets = secretManagerService.getSecret("CUSTOMER_GALLABOX");
                    }
                }
            }
            return customerGallaboxSecrets;
        } else {
            if (advisorGallaboxSecrets == null) {
                synchronized (lock) {
                    if (advisorGallaboxSecrets == null) {
                        advisorGallaboxSecrets = secretManagerService.getSecret("ADVISOR_GALLABOX");
                    }
                }
            }
            return advisorGallaboxSecrets;
        }
    }

    public String getApiKey(String recipientType) {
        Map<String, Object> secrets = getSecretsForRecipient(recipientType);
        return secrets.get("apiKey").toString();
    }

    public String getApiSecret(String recipientType) {
        Map<String, Object> secrets = getSecretsForRecipient(recipientType);
        return secrets.get("apiSecret").toString();
    }

    public String getChannelId(String recipientType) {
        Map<String, Object> secrets = getSecretsForRecipient(recipientType);
        return secrets.get("channelId").toString();
    }
}
