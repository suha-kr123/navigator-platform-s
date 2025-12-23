package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code ADVISOR_CALL_LOG_UPDATED} events.
 */
@Value
@Builder
public class AdvisorCallLogUpdateEventPayload {

    Long advisorId;

    Long callLogId;

    UUID callLogIdentifier;

}