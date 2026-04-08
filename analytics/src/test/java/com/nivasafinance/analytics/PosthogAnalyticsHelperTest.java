package com.nivasafinance.analytics;

import com.nivasafinance.common.context.UserContext;
import com.posthog.server.PostHogCaptureOptions;
import com.posthog.server.PostHogInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosthogAnalyticsHelperTest {

    private static final String TEST_USERNAME = "test-user";
    private static final String TEST_DISTINCT_ID = "12345";
    private static final String TEST_EVENT_TYPE = "LEAD_CREATED";

    @Mock
    private PostHogInterface posthog;

    @InjectMocks
    private PosthogAnalyticsHelper posthogAnalyticsHelper;

    @BeforeEach
    void setUp() {
        UserContext.setUsername(TEST_USERNAME);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ── captureLead ──

    @Test
    void captureLead_withValidEvent_capturesWithLeadPrefix() {
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, TEST_EVENT_TYPE);

        posthogAnalyticsHelper.captureLead(event);

        ArgumentCaptor<String> distinctIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(posthog).capture(distinctIdCaptor.capture(), eq(TEST_EVENT_TYPE), any(PostHogCaptureOptions.class));
        assertEquals("LEAD-" + TEST_DISTINCT_ID, distinctIdCaptor.getValue(),
                "captureLead should prefix distinctId with 'LEAD-'");
    }

    @Test
    void captureLead_withEmptyExtras_callsCaptureOnce() {
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, TEST_EVENT_TYPE);

        posthogAnalyticsHelper.captureLead(event);

        verify(posthog, times(1)).capture(anyString(), anyString(), any(PostHogCaptureOptions.class));
    }

    @Test
    void captureLead_passesEventTypeToCapture() {
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, "CUSTOM_EVENT");

        posthogAnalyticsHelper.captureLead(event);

        verify(posthog).capture(anyString(), eq("CUSTOM_EVENT"), any(PostHogCaptureOptions.class));
    }

    // ── captureAdvisor ──

    @Test
    void captureAdvisor_withValidEvent_capturesWithAdvisorPrefix() {
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, TEST_EVENT_TYPE);

        posthogAnalyticsHelper.captureAdvisor(event);

        ArgumentCaptor<String> distinctIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(posthog).capture(distinctIdCaptor.capture(), eq(TEST_EVENT_TYPE), any(PostHogCaptureOptions.class));
        assertEquals("ADVISOR-" + TEST_DISTINCT_ID, distinctIdCaptor.getValue(),
                "captureAdvisor should prefix distinctId with 'ADVISOR-'");
    }

    @Test
    void captureAdvisor_passesEventTypeToCapture() {
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, "ADVISOR_SIGNUP");

        posthogAnalyticsHelper.captureAdvisor(event);

        verify(posthog).capture(anyString(), eq("ADVISOR_SIGNUP"), any(PostHogCaptureOptions.class));
    }

    // ── capture (private, tested via captureLead/captureAdvisor) – extras filtering ──

    @Test
    void captureLead_withExtras_filtersOutDistinctIdParam() {
        List<AnalyticsEvent.Param> extras = List.of(
                new AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.DISTINCT_ID, "should-be-filtered"),
                new AnalyticsEvent.Param("source", "web")
        );
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, TEST_EVENT_TYPE, extras);

        posthogAnalyticsHelper.captureLead(event);

        verify(posthog).capture(eq("LEAD-" + TEST_DISTINCT_ID), eq(TEST_EVENT_TYPE), any(PostHogCaptureOptions.class));
    }

    @Test
    void captureAdvisor_withMultipleExtras_callsCaptureOnce() {
        List<AnalyticsEvent.Param> extras = List.of(
                new AnalyticsEvent.Param("key1", "val1"),
                new AnalyticsEvent.Param("key2", "val2"),
                new AnalyticsEvent.Param("key3", "val3")
        );
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, TEST_EVENT_TYPE, extras);

        posthogAnalyticsHelper.captureAdvisor(event);

        verify(posthog, times(1)).capture(anyString(), anyString(), any(PostHogCaptureOptions.class));
    }

    // ── UserContext ──

    @Test
    void captureLead_whenUserContextHasUsername_callsCapture() {
        UserContext.setUsername("specific-user");
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, TEST_EVENT_TYPE);

        posthogAnalyticsHelper.captureLead(event);

        verify(posthog).capture(eq("LEAD-" + TEST_DISTINCT_ID), eq(TEST_EVENT_TYPE), any(PostHogCaptureOptions.class));
    }

    @Test
    void captureLead_whenUserContextIsNull_stillCallsCapture() {
        UserContext.clear();
        AnalyticsEvent event = new AnalyticsEvent(TEST_DISTINCT_ID, TEST_EVENT_TYPE);

        posthogAnalyticsHelper.captureLead(event);

        verify(posthog).capture(eq("LEAD-" + TEST_DISTINCT_ID), eq(TEST_EVENT_TYPE), any(PostHogCaptureOptions.class));
    }
}
