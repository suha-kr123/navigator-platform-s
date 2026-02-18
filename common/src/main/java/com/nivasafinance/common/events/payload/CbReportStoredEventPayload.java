package com.nivasafinance.common.events.payload;

import lombok.Builder;
import lombok.Value;

/**
 * Payload published with {@code CB_REPORT_STORED} events.
 * Lead module uses enquiryId to fetch Redash Excel report and upload to the lead.
 */
@Value
@Builder
public class CbReportStoredEventPayload {

    Long enquiryId;
}
