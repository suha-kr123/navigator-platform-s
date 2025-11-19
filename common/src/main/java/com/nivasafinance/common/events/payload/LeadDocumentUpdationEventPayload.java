package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_DOCUMENT_UPDATED} events.
 */
@Value
@Builder
public class LeadDocumentUpdationEventPayload {

    Long leadId;

    Long documentId;

    UUID documentIdentifier;

}

