package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_CONTACT_CREATED} events.
 */
@Value
@Builder
public class LeadContactCreationEventPayload {

    Long leadId;

    Long contactId;

    UUID contactIdentifier;

    String contactType;

    Boolean isDecisionMaker;

    Boolean isPropertyOwner;

}

