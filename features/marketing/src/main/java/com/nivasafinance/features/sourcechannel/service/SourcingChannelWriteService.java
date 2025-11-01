package com.nivasafinance.features.sourcechannel.service;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;

public interface SourcingChannelWriteService {
    SourcingChannelResponse create(SourcingChannelRequest request);
    SourcingChannelResponse update(Long id, SourcingChannelRequest request);
}

