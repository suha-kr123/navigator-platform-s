package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourcingDetailsResponse {
    private SourcingChannelResponse sourcingChannelDetails;
}
