package com.nivasafinance.analytics;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of {@link AnalyticsHelper} used when analytics is disabled (e.g. dev profile).
 */
@Component
@Profile("dev")
public class NoOPAnalyticsHelper implements AnalyticsHelper {

    @Override
    public void captureLead(AnalyticsEvent event) {

    }

    @Override
    public void captureAdvisor(AnalyticsEvent event) {

    }
}
