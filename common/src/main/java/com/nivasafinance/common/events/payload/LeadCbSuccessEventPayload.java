package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

/**
 * Payload published with {@code LEAD_CB_PULL_SUCCESS} events.
 * Lead module uses enquiryId to fetch Redash Excel report and upload to the lead.
 */
@Value
@Builder
public class LeadCbSuccessEventPayload {

    Long enquiryId;
    Long leadId;
}
