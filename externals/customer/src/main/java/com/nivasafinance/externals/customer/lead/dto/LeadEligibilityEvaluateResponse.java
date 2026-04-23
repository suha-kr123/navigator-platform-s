package com.nivasafinance.externals.customer.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadEligibilityEvaluateResponse {
    private UUID leadIdentifier;
    private LeadStatus leadStatus;
}
