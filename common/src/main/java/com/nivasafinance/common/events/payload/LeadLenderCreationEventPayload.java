package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_LENDER_CREATED} events.
 */
@Value
@Builder
public class LeadLenderCreationEventPayload {

    Long leadId;

    Long lenderId;

    UUID lenderIdentifier;

}

