package com.nivasafinance.analytics;

import java.util.List;

/**
 * Represents an analytics event.
 *
 * @param type   the event type. Wherever possible use one of the standard event {@code Types},
 *               however, if there is no suitable event type already defined, a custom event can be
 *               defined as long as it is configured in your backend analytics system (for example,
 *               by creating a Firebase Analytics custom event).
 * @param extras list of parameters which supply additional context to the event. See {@link Param}.
 */
public record AnalyticsEvent(
        String distinctId,
        String type,
        List<Param> extras
) {
    public AnalyticsEvent {
        extras = extras != null ? extras : List.of();
    }

    public AnalyticsEvent(String distinctId, String type) {
        this(distinctId, type, List.of());
    }

    /**
     * A key-value pair used to supply extra context to an analytics event.
     *
     * @param key   the parameter key. Wherever possible use one of the standard {@link ParamKeys},
     *              however, if no suitable key is available you can define your own as long as it is
     *              configured in your backend analytics system (for example, by creating a Firebase
     *              Analytics custom parameter).
     * @param value the parameter value.
     */
    public record Param(String key, String value) {
    }

    /**
     * Standard parameter keys for analytics events.
     */
    public static final class ParamKeys {
        private ParamKeys() {
        }

        /**
         * Distinct identifier for the user/session.
         */
        public static final String DISTINCT_ID = "distinct_id";
    }
}
