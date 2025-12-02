package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with advisor status change events (ADVISOR_REJECTED, ADVISOR_DORMANT, ADVISOR_ACTIVE).
 */
@Value
@Builder
public class AdvisorStatusChangeEventPayload {

    Long id;

    UUID advisorIdentifier;

    String reason;

}

