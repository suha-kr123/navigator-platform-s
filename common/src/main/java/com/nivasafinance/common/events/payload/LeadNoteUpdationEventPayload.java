package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_NOTE_UPDATED} events.
 */
@Value
@Builder
public class LeadNoteUpdationEventPayload {

    Long leadId;

    Long noteId;

    UUID noteIdentifier;

}

