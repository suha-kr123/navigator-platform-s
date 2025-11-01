package com.nivasafinance.features.sourcechannel.service;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;

public interface SourcingChannelReadService {
    SourcingChannelResponse getById(Long id);
    SourcingChannelResponse getBySourcingIdentifier(String sourcingIdentifier);
}
