package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_CREATED} events.
 */
@Value
@Builder
public class LeadCreationEventPayload {

    UUID leadId;

    String mobileNumber;
}


