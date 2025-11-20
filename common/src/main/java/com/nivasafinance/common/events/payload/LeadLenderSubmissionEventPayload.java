package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_LENDER_SUBMITTED} events.
 */
@Value
@Builder
public class LeadLenderSubmissionEventPayload {

    Long leadId;

    Long lenderId;

    UUID lenderIdentifier;

}

