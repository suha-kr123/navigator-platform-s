package com.nivasafinance.notification.executor.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** Initializes Firebase Admin SDK at startup from config/secret. */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessagingConfig {

    private final FirebaseConfigProvider firebaseConfigProvider;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        ThirdPartyConfig config = firebaseConfigProvider.getConfig();
        Map<String, String> configMap = config.getConfigurations();

        GoogleCredentials credentials;
        if (configMap.containsKey("service_account_path")) {
            String path = configMap.get("service_account_path");
            log.info("Loading Firebase service account from file: {}", path);
            try (FileInputStream stream = new FileInputStream(path)) {
                credentials = GoogleCredentials.fromStream(stream);
            }
        } else if (configMap.containsKey("service_account_json")) {
            log.info("Loading Firebase service account from JSON string");
            try (InputStream stream = new ByteArrayInputStream(configMap.get("service_account_json").getBytes())) {
                credentials = GoogleCredentials.fromStream(stream);
            }
        } else {
            throw new IllegalStateException("Firebase service account not configured. " +
                    "Provide firebase.service-account.path or firebase.service-account.json or FIREBASE_SERVICE_ACCOUNT secret.");
        }

        FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("Firebase Admin SDK initialized at startup. App name: {}", app.getName());
        return FirebaseMessaging.getInstance(app);
    }
}
