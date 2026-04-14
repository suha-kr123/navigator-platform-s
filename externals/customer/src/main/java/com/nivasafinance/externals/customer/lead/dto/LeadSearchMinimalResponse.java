package com.nivasafinance.externals.customer.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSearchMinimalResponse {
    private UUID leadIdentifier;
    private String primaryPersonName;
    private LeadStatus status;
    private LeadSubStatus subStatus;
}
