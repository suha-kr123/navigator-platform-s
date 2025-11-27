package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code ADVISOR_CREATED} events.
 */
@Value
@Builder
public class AdvisorCreationEventPayload {

    Long id;

    UUID advisorIdentifier;

    String mobileNumber;
}
