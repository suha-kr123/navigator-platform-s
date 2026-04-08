package com.nivasafinance.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NoOPAnalyticsHelperTest {

    private final NoOPAnalyticsHelper noOPAnalyticsHelper = new NoOPAnalyticsHelper();

    // ── captureLead ──

    @Test
    void captureLead_withValidEvent_doesNotThrow() {
        AnalyticsEvent event = new AnalyticsEvent("id-1", "LOGIN");

        assertDoesNotThrow(() -> noOPAnalyticsHelper.captureLead(event),
                "No-op captureLead should complete without throwing");
    }

    @Test
    void captureLead_withNullEvent_doesNotThrow() {
        assertDoesNotThrow(() -> noOPAnalyticsHelper.captureLead(null),
                "No-op captureLead should handle null event without throwing");
    }

    // ── captureAdvisor ──

    @Test
    void captureAdvisor_withValidEvent_doesNotThrow() {
        AnalyticsEvent event = new AnalyticsEvent("id-1", "SIGNUP");

        assertDoesNotThrow(() -> noOPAnalyticsHelper.captureAdvisor(event),
                "No-op captureAdvisor should complete without throwing");
    }

    @Test
    void captureAdvisor_withNullEvent_doesNotThrow() {
        assertDoesNotThrow(() -> noOPAnalyticsHelper.captureAdvisor(null),
                "No-op captureAdvisor should handle null event without throwing");
    }
}
