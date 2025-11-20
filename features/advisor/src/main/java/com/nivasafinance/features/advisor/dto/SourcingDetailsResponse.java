package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sourcing channel details response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourcingDetailsResponse {
    private SourcingChannelResponse sourcingChannelDetails;
}
