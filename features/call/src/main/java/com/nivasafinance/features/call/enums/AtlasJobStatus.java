package com.nivasafinance.features.call.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Atlas transcription / AI analysis job lifecycle for {@link com.nivasafinance.features.call.entity.CallLog.AiAnalysisDetails}.
 * Serialized as lowercase wire values ({@code initiated}, {@code processing}, {@code completed}, {@code failed},
 * {@code publishing_failed}).
 */
public enum AtlasJobStatus {

    INITIATED("initiated"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed"),
    /** Local enqueue to SQS failed; a later event may retry. */
    PUBLISHING_FAILED("publishing_failed");

    private final String wireValue;

    AtlasJobStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String getWireValue() {
        return wireValue;
    }

    /**
     * Parses Atlas / stored JSON values; maps legacy uppercase synonyms where applicable.
     *
     * @return {@code null} if blank or unrecognized
     */
    public static AtlasJobStatus fromWireOrLegacy(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "initiated", "pending", "queued" -> INITIATED;
            case "processing" -> PROCESSING;
            case "completed" -> COMPLETED;
            case "failed" -> FAILED;
            case "publishing_failed" -> PUBLISHING_FAILED;
            default -> null;
        };
    }
}
