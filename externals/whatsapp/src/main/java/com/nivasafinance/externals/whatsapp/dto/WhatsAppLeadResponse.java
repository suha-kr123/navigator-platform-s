package com.nivasafinance.externals.whatsapp.dto;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppLeadResponse {
    private UUID leadIdentifier;
    private UUID contactIdentifier;
    private List<AddressData> address;
    private PreliminaryDetailsResponse preliminaryDetails;
    private LeadStatus status;
    private String stage;
}
