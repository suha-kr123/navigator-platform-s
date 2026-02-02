package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_PRELIMINARY_DETAILS_UPDATED} events.
 */
@Value
@Builder
public class LeadPreliminaryDetailsUpdateEventPayload {
    
    Long leadId;

    UUID leadIdentifier;

}
