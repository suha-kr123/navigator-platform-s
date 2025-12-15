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
public class WatiConfig {

    private final SecretManagerService secretManagerService;
    private Map<String, Object> watiSecrets;

    @PostConstruct
    public void init() {
        this.watiSecrets = secretManagerService.getSecret("CUSTOMER_WATI");
    }

    public String getApiKey() {
        return watiSecrets.get("api_key").toString();
    }

    public String getBaseUrl() {
        return watiSecrets.get("base_url").toString();
    }

    public String getSenderId() {
        return watiSecrets.get("sender_id").toString();
    }
}

