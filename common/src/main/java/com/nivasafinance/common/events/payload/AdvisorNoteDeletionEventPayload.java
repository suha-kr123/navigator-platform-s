package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code ADVISOR_NOTE_DELETED} events.
 */
@Value
@Builder
public class AdvisorNoteDeletionEventPayload {

    Long advisorId;

    Long noteId;

    UUID noteIdentifier;

}

