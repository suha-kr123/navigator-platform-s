package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.call.dto.CallLogResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadCallLogResponse {
    private CallLogResponse callLogDetails;
    /** From {@code n_call_log_lead.contact_id} when the call is linked to a lead contact. */
    private Long contactId;
}
