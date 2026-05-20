package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.common.enums.ReferredByType;
import com.nivasafinance.features.advisor.entity.Advisor;
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
    private List<Advisor.SourcingEntry> sourcingHistory;
    private String referredByCode;
    private ReferredByType referredByType;
}
