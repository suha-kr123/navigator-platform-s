package com.nivasafinance.common.events.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code CB_CONSENT_SENT} events.
 * Used by notifications to send the consent link (e.g. WhatsApp/SMS).
 * Field names match notification data provider / template expectations.
 */
@Value
@Builder
public class ConsentSentEventPayload {

    Long personId;

    Long consentId;

    UUID consentIdentifier;

    UUID enquiryIdentifier;

    @JsonProperty("consent_link")
    String consentLink;

    /**
     * Recipient contact (e.g. phone) for the notification. Must match data provider / template.
     */
    @JsonProperty("recipient_contact")
    String recipientContact;
}
