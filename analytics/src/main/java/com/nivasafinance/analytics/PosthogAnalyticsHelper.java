package com.nivasafinance.analytics;

import com.posthog.server.PostHogCaptureOptions;
import com.posthog.server.PostHogInterface;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


/**
 * PostHog implementation of {@link AnalyticsHelper} for production.
 */
@Component
@Profile("prod")
public class PosthogAnalyticsHelper implements AnalyticsHelper {

    private final PostHogInterface posthog;

    public PosthogAnalyticsHelper(PostHogInterface posthog) {
        this.posthog = posthog;
    }

    @Override
    public void captureLead(AnalyticsEvent event) {
        capture("LEAD-" + event.distinctId(), event);
    }

    @Override
    public void captureAdvisor(AnalyticsEvent event) {
        capture("ADVISOR-" + event.distinctId(), event);
    }

    private void capture(String distinctId, AnalyticsEvent event) {
        PostHogCaptureOptions.Builder optionsBuilder = PostHogCaptureOptions.builder();
        event.extras().stream()
                .filter(p -> !AnalyticsEvent.ParamKeys.DISTINCT_ID.equals(p.key()))
                .forEach(p -> optionsBuilder.property(p.key(), p.value()));
        posthog.capture(distinctId, event.type(), optionsBuilder.build());
    }
}
