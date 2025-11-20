package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_DOCUMENT_DELETED} events.
 */
@Value
@Builder
public class LeadDocumentDeletionEventPayload {

    Long leadId;

    Long documentId;

    UUID documentIdentifier;

}

