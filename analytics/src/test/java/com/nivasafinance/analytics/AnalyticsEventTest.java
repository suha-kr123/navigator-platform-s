package com.nivasafinance.analytics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsEventTest {

    // ── Three-arg constructor ──

    @Test
    void constructor_withNullExtras_defaultsToEmptyList() {
        AnalyticsEvent event = new AnalyticsEvent("id-1", "LOGIN", null);

        assertNotNull(event.extras(), "Extras should never be null even when constructed with null");
        assertTrue(event.extras().isEmpty(), "Extras should default to empty list when null is provided");
    }

    @Test
    void constructor_withValidExtras_retainsAllParams() {
        List<AnalyticsEvent.Param> params = List.of(
                new AnalyticsEvent.Param("key1", "value1"),
                new AnalyticsEvent.Param("key2", "value2")
        );

        AnalyticsEvent event = new AnalyticsEvent("id-1", "LOGIN", params);

        assertEquals(2, event.extras().size(), "All provided params should be retained in extras");
        assertEquals("key1", event.extras().get(0).key(), "First param key should match");
        assertEquals("value1", event.extras().get(0).value(), "First param value should match");
    }

    @Test
    void constructor_setsDistinctIdAndType() {
        AnalyticsEvent event = new AnalyticsEvent("user-42", "SIGNUP", List.of());

        assertEquals("user-42", event.distinctId(), "DistinctId should match the provided value");
        assertEquals("SIGNUP", event.type(), "Type should match the provided value");
    }

    // ── Two-arg constructor ──

    @Test
    void twoArgConstructor_createsEventWithEmptyExtras() {
        AnalyticsEvent event = new AnalyticsEvent("id-1", "LOGOUT");

        assertNotNull(event.extras(), "Extras should not be null for two-arg constructor");
        assertTrue(event.extras().isEmpty(), "Two-arg constructor should create event with empty extras");
    }

    @Test
    void twoArgConstructor_setsDistinctIdAndType() {
        AnalyticsEvent event = new AnalyticsEvent("user-99", "PAGE_VIEW");

        assertEquals("user-99", event.distinctId(), "DistinctId should match for two-arg constructor");
        assertEquals("PAGE_VIEW", event.type(), "Type should match for two-arg constructor");
    }

    // ── Param record ──

    @Test
    void param_storesKeyAndValue() {
        AnalyticsEvent.Param param = new AnalyticsEvent.Param("source", "mobile");

        assertEquals("source", param.key(), "Param key should match the provided key");
        assertEquals("mobile", param.value(), "Param value should match the provided value");
    }

    // ── ParamKeys ──

    @Test
    void paramKeys_distinctIdConstant_hasExpectedValue() {
        assertEquals("distinct_id", AnalyticsEvent.ParamKeys.DISTINCT_ID,
                "DISTINCT_ID constant should be 'distinct_id'");
    }
}
