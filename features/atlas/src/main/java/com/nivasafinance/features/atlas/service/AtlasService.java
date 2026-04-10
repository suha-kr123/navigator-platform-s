package com.nivasafinance.features.atlas.service;

import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;
import com.nivasafinance.common.events.payload.StageTransitionEventPayload;

public interface AtlasService {

    void handleLeadCallLogCreated(LeadCallLogCreationEventPayload payload);

    void handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload payload);

    /**
     * Re-evaluates Atlas enqueue for all call logs linked to the lead after a workflow stage change
     * (e.g. lead moved into a stage where {@code stage_eligible} becomes true).
     */
    void handleLeadStageTransitioned(StageTransitionEventPayload payload);
}
