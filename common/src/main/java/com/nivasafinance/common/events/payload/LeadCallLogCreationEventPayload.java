package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_CALL_LOG_CREATED} events.
 */
@Value
@Builder
public class LeadCallLogCreationEventPayload {

    Long leadId;

    Long callLogId;

    UUID callLogIdentifier;

    /** lead identifier; required for Atlas queue payload. */
    UUID leadIdentifier;

    /** Primary role of the publishing user at event time. */
    String primaryRole;

}

