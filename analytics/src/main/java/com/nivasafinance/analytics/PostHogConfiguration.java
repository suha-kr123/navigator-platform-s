package com.nivasafinance.analytics;

import com.posthog.server.PostHog;
import com.posthog.server.PostHogConfig;
import com.posthog.server.PostHogInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.posthog.server.PostHogConfig.DEFAULT_EU_HOST;

@Configuration
public class PostHogConfiguration {

    @Value("${posthog.api.key:}")
    private String apiKey;

    @Bean(destroyMethod = "close")
    public PostHogInterface posthog() {
        PostHogConfig config = PostHogConfig
                .builder(apiKey)
                .host(DEFAULT_EU_HOST)
                .build();
        return PostHog.with(config);
    }
}