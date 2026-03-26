package com.nivasafinance.features.atlas.service;

import com.nivasafinance.common.events.payload.LeadCallLogCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;

public interface AtlasService {

    void handleLeadCallLogCreated(LeadCallLogCreationEventPayload payload);

    void handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload payload);
}
