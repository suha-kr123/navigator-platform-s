package com.nivasafinance.features.sourcechannel.service.impl;

import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
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
    private final CodeValueMasterService codeValueMasterService;

    @Override
    public SourcingChannelResponse create(SourcingChannelRequest request) {

        if(request.getSourcingChannel() != null){ //validate
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getSourcingChannel(), SystemControlledMasterCodes.MARKETING_SOURCE_MASTER);
        }

        if(request.getMarketingSource() != null){ //validate
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getMarketingSource(), SystemControlledMasterCodes.MARKETING_CHANNEL_MASTER);
        }

        SourcingChannel.SourcingChannelBuilder builder = SourcingChannel.builder()
                .sourcingIdentifier(UUID.randomUUID())
                .sourcingChannel(request.getSourcingChannel())
                .marketingSource(request.getMarketingSource());
        if (request.getMarketingDetails() != null) {
            builder.marketingDetails(SourcingChannel.MarketingDetails
                    .builder()
                    .sourceId(request.getMarketingDetails().getSourceId())
                    .sourceUrl(request.getMarketingDetails().getSourceUrl())
                    .campaignId(request.getMarketingDetails().getCampaignId())
                    .sourcedBy(request.getMarketingDetails().getSourcedBy())
                    .build());
        }
        SourcingChannel sourcingChannel = builder.build();
        SourcingChannel savedEntity = sourcingChannelRepositoryWrapper.saveWithException(sourcingChannel);
        return sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(savedEntity.getId());
    }

    @Override
    public SourcingChannelResponse update(Long id, SourcingChannelRequest request) {
        SourcingChannel existingEntity = sourcingChannelRepositoryWrapper.findByIdWithException(id);

        if(request.getSourcingChannel() != null){ 
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getSourcingChannel(), SystemControlledMasterCodes.MARKETING_SOURCE_MASTER);
        }

        if(request.getMarketingSource() != null){ 
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getMarketingSource(), SystemControlledMasterCodes.MARKETING_CHANNEL_MASTER);
        }

        existingEntity.setSourcingChannel(request.getSourcingChannel());
        existingEntity.setMarketingSource(request.getMarketingSource());
        if (request.getMarketingDetails() != null) {
            SourcingChannel.MarketingDetails marketingDetails = existingEntity.getMarketingDetails();
            if (marketingDetails == null) {
                marketingDetails = new SourcingChannel.MarketingDetails();
            }
            marketingDetails.setSourceId(request.getMarketingDetails().getSourceId());
            marketingDetails.setSourceUrl(request.getMarketingDetails().getSourceUrl());
            marketingDetails.setCampaignId(request.getMarketingDetails().getCampaignId());
            marketingDetails.setSourcedBy(request.getMarketingDetails().getSourcedBy());
            existingEntity.setMarketingDetails(marketingDetails);
        } else {
            existingEntity.setMarketingDetails(null);
        }

        SourcingChannel savedEntity = sourcingChannelRepositoryWrapper.saveWithException(existingEntity);
        return sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(savedEntity.getId());
    }
}

