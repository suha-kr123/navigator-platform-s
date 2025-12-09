package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with lead status change events (LEAD_REJECTED, LEAD_REJECTION_UNDO, LEAD_WITHDRAWN, LEAD_ON_HOLD, LEAD_RESUMED, LEAD_COMPLETED, LEAD_DROPOFF).
 */
@Value
@Builder
public class LeadStatusChangeEventPayload {

    Long leadId;

    UUID leadIdentifier;

    String reason;

}

