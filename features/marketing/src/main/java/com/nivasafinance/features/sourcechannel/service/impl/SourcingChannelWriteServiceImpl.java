package com.nivasafinance.features.sourcechannel.service.impl;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.entity.SourcingChannel;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class SourcingChannelWriteServiceImpl implements SourcingChannelWriteService {

    private final SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;

    @Override
    public SourcingChannelResponse create(SourcingChannelRequest request) {

        SourcingChannel.SourcingChannelBuilder builder = SourcingChannel.builder()
                .sourcingIdentifier(UUID.randomUUID())
                .sourcingChannel(request.getSourcingChannel())
                .marketingSource(request.getMarketingSource());
        if (request.getMarketingDetails() != null) {
            builder.marketingDetails(SourcingChannel.MarketingDetails
                    .builder()
                    .sourceId(request.getMarketingDetails().getSourceId())
                    .build());
        }
        SourcingChannel sourcingChannel = builder.build();
        SourcingChannel savedEntity = sourcingChannelRepositoryWrapper.saveWithException(sourcingChannel);
        return sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(savedEntity.getId());
    }

    @Override
    public SourcingChannelResponse update(Long id, SourcingChannelRequest request) {
        SourcingChannel existingEntity = sourcingChannelRepositoryWrapper.findByIdWithException(id);

        existingEntity.setSourcingChannel(request.getSourcingChannel());
        existingEntity.setMarketingSource(request.getMarketingSource());
        if (request.getMarketingDetails() != null) {
            SourcingChannel.MarketingDetails marketingDetails = existingEntity.getMarketingDetails();
            if(marketingDetails == null){
                marketingDetails = new SourcingChannel.MarketingDetails();
            }
            marketingDetails.setSourceId(request.getMarketingDetails().getSourceId());
        }else {
            existingEntity.setMarketingDetails(null);
        }

        SourcingChannel savedEntity = sourcingChannelRepositoryWrapper.saveWithException(existingEntity);
        return sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(savedEntity.getId());
    }
}

