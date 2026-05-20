package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.enums.ReferredByType;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourcingDetailsResponse {
    @Deprecated
    private SourcingChannelResponse sourcingChannelDetails;
    private List<Lead.SourcingEntry> sourcingHistory;
    private String referredByCode;
    private ReferredByType referredByType;
}
