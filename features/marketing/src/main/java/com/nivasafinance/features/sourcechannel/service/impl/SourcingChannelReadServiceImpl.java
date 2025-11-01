package com.nivasafinance.features.sourcechannel.service.impl;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class SourcingChannelReadServiceImpl implements SourcingChannelReadService {

    private final SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;

    @Override
    public SourcingChannelResponse getById(Long id) {
        return sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(id);
    }

    @Override
    public SourcingChannelResponse getBySourcingIdentifier(String sourcingIdentifier) {
        return sourcingChannelRepositoryWrapper.findBySourcingIdentifierAsResponseWithException(sourcingIdentifier);
    }
}
