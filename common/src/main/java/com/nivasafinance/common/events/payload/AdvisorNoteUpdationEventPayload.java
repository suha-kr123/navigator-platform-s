package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code ADVISOR_NOTE_UPDATED} events.
 */
@Value
@Builder
public class AdvisorNoteUpdationEventPayload {

    Long advisorId;

    Long noteId;

    UUID noteIdentifier;

}

