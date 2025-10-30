package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.entity.Lead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadResponse {
    private UUID leadIdentifier;

    public static LeadResponse leadToResponse(Lead lead) {
        LeadResponse.LeadResponseBuilder builder = LeadResponse.builder()
                .leadIdentifier(lead.getLeadIdentifier());
        return builder.build();
    }
}
