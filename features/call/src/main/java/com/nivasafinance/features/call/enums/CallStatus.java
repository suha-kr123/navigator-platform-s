package com.nivasafinance.features.call.enums;

import com.nivasafinance.services.voice.dto.VoiceStatus;

public enum CallStatus {
    QUEUED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    BUSY,
    NO_ANSWER,

    ;

    public static CallStatus fromVoiceStatus(VoiceStatus voiceStatus) {
        return switch (voiceStatus) {
            case QUEUED -> CallStatus.QUEUED;
            case IN_PROGRESS -> CallStatus.IN_PROGRESS;
            case COMPLETED -> CallStatus.COMPLETED;
            case FAILED -> CallStatus.FAILED;
            case BUSY -> CallStatus.BUSY;
            case NO_ANSWER -> CallStatus.NO_ANSWER;
        };
    }
}
