package com.nivasafinance.analytics;

/**
 * Sends analytics events to the configured backend (e.g. PostHog in prod, no-op in dev).
 */
public interface AnalyticsHelper {

    /**
     * Captures an analytics event. The event must include a {@link AnalyticsEvent.ParamKeys#DISTINCT_ID}
     * in extras when the backend requires it (e.g. PostHog).
     *
     * @param event the analytics event to capture
     */
    void captureLead(AnalyticsEvent event);

    void captureAdvisor(AnalyticsEvent event);
}
