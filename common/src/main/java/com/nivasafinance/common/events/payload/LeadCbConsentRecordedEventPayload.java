package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_CB_CONSENT_RECORDED} events.
 * Carries lead and contact identifiers so the listener can initiate the CB enquiry and CRIF pull.
 */
@Value
@Builder
public class LeadCbConsentRecordedEventPayload {

    UUID leadIdentifier;
    UUID contactIdentifier;
}
