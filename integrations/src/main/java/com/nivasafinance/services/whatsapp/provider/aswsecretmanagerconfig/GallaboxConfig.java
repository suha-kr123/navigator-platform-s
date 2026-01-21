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
    private Map<String, Object> customerGallaboxSecrets;
    private Map<String, Object> advisorGallaboxSecrets;

    @PostConstruct
    public void init() {
        this.customerGallaboxSecrets = secretManagerService.getSecret("CUSTOMER_GALLABOX");
        this.advisorGallaboxSecrets = secretManagerService.getSecret("ADVISOR_GALLABOX");
    }

    public String getApiKey(String recipientType) {
        boolean isCustomer = "LEAD".equalsIgnoreCase(recipientType);
        Map<String, Object> secrets = isCustomer ? customerGallaboxSecrets : advisorGallaboxSecrets;
        return secrets.get("apiKey").toString();
    }

    public String getApiSecret(String recipientType) {
        boolean isCustomer = "LEAD".equalsIgnoreCase(recipientType);
        Map<String, Object> secrets = isCustomer ? customerGallaboxSecrets : advisorGallaboxSecrets;
        return secrets.get("apiSecret").toString();
    }

    public String getChannelId(String recipientType) {
        boolean isCustomer = "LEAD".equalsIgnoreCase(recipientType);
        Map<String, Object> secrets = isCustomer ? customerGallaboxSecrets : advisorGallaboxSecrets;
        return secrets.get("channelId").toString();
    }
}
