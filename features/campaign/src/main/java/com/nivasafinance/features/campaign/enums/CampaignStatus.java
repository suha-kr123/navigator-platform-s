package com.nivasafinance.features.campaign.enums;

import com.nivasafinance.services.voice.dto.VoiceCampaignStatus;

public enum CampaignStatus {
    DRAFT,
    CANCELLED,
    SUBMISSION_IN_PROGRESS,
    SUBMITTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    PAUSED,
    ;

    public boolean canBeSubmitted() {
        return this == DRAFT || this == FAILED;
    }

    public boolean canRefresh() {
        return this == SUBMITTED || this == IN_PROGRESS || this == PAUSED;
    }

    public static CampaignStatus fromVoiceCampaignStatus(VoiceCampaignStatus voiceStatus) {
        if (voiceStatus == null) {
            return SUBMITTED; // Default to SUBMITTED if status is null
        }
        return switch (voiceStatus) {
            case CREATED -> SUBMITTED; // When provider creates campaign, it's submitted
            case IN_PROGRESS -> IN_PROGRESS;
            case COMPLETED -> COMPLETED;
            case FAILED -> FAILED;
            case CANCELLED -> CANCELLED;
            case PAUSED -> PAUSED;
        };
    }
}
