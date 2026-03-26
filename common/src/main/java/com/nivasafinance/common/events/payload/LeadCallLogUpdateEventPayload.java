package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Payload published with {@code LEAD_CALL_LOG_UPDATED} events.
 */
@Value
@Builder
public class LeadCallLogUpdateEventPayload {

    Long leadId;

    Long callLogId;

    UUID callLogIdentifier;

    UUID leadIdentifier;

    /**
     * Recording URL on the call log after update, if any. Atlas and similar consumers run only when this is present.
     */
    String recordingUrl;

    /**
     * Primary role key for the user associated with the update (e.g. CSE, SME). Atlas uses the call log
     * staff leg ({@code toNumber} inbound, {@code fromNumber} outbound) when resolvable, and falls back to this.
     */
    String primaryRole;

}
