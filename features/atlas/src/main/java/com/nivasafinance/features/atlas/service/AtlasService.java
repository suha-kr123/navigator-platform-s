package com.nivasafinance.features.atlas.service;

import com.nivasafinance.common.events.payload.LeadCallLogUpdateEventPayload;

public interface AtlasService {

    void handleLeadCallLogUpdated(LeadCallLogUpdateEventPayload payload);
}
