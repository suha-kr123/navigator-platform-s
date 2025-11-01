package com.nivasafinance.features.sourcechannel.dto;

import com.nivasafinance.features.sourcechannel.entity.SourcingChannel;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourcingChannelResponse {
    private Long id;
    private UUID sourcingIdentifier;
    private CodeValueResponse sourcingChannel;
    private CodeValueResponse marketingSource;
    private SourcingChannel.MarketingDetails marketingDetails;
}
