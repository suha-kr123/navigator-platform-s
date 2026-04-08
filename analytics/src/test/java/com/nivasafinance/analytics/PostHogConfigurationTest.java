package com.nivasafinance.analytics;

import com.posthog.server.PostHogInterface;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class PostHogConfigurationTest {

    // ── posthog bean creation ──

    @Test
    void posthog_withApiKey_returnsNonNullInstance() {
        PostHogConfiguration configuration = new PostHogConfiguration();
        ReflectionTestUtils.setField(configuration, "apiKey", "phc_test_key_123");

        PostHogInterface result = configuration.posthog();

        assertNotNull(result, "PostHogInterface bean should be created when API key is provided");
    }

    @Test
    void posthog_withEmptyApiKey_returnsNonNullInstance() {
        PostHogConfiguration configuration = new PostHogConfiguration();
        ReflectionTestUtils.setField(configuration, "apiKey", "");

        PostHogInterface result = configuration.posthog();

        assertNotNull(result, "PostHogInterface bean should be created even with empty API key");
    }
}
