package com.nivasafinance.externals.lead.dto;

import com.nivasafinance.features.lead.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeadResponse {
    private UUID leadIdentifier;
    private UUID contactIdentifier;
}
