package com.nivasafinance.features.sourcechannel.repository;

import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.entity.SourcingChannel;
import com.nivasafinance.features.sourcechannel.exception.SourcingChannelExceptionFactory;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class SourcingChannelRepositoryWrapper {

    private final SourcingChannelRepository sourcingChannelRepository;
    private final MessageSource messageSource;
    private final CodeValueMasterService codeValueMasterService;

    public SourcingChannel saveWithException(SourcingChannel sourcingChannel) {
        try {
            return sourcingChannelRepository.save(sourcingChannel);
        } catch (DataAccessException e) {
            throw SourcingChannelExceptionFactory.createFailed(messageSource);
        }
    }

    public SourcingChannel findByIdWithException(Long id) {
        return sourcingChannelRepository.findById(id)
                .orElseThrow(() -> SourcingChannelExceptionFactory.notFound(id, messageSource));
    }

    public SourcingChannel findBySourcingIdentifierWithException(String sourcingIdentifier) {
        return sourcingChannelRepository.findBySourcingIdentifier(UUID.fromString(sourcingIdentifier))
                .orElseThrow(() -> SourcingChannelExceptionFactory.notFound(sourcingIdentifier, messageSource));
    }

    public SourcingChannelResponse findByIdAsResponseWithException(Long id) {
        SourcingChannel sourcingChannel = findByIdWithException(id);
        return toResponse(sourcingChannel);
    }

    public SourcingChannelResponse findBySourcingIdentifierAsResponseWithException(String sourcingIdentifier) {
        SourcingChannel sourcingChannel = findBySourcingIdentifierWithException(sourcingIdentifier);
        return toResponse(sourcingChannel);
    }

    private SourcingChannelResponse toResponse(SourcingChannel sourcingChannel) {
        CodeValueResponse sourcingChannelCodeValue = sourcingChannel.getSourcingChannel() != null 
            ? codeValueMasterService.getByKey(sourcingChannel.getSourcingChannel()) 
            : null;
        CodeValueResponse marketingSourceCodeValue = sourcingChannel.getMarketingSource() != null 
            ? codeValueMasterService.getByKey(sourcingChannel.getMarketingSource()) 
            : null;

        return new SourcingChannelResponse(
                sourcingChannel.getId(),
                sourcingChannel.getSourcingIdentifier(),
                sourcingChannelCodeValue,
                marketingSourceCodeValue,
                sourcingChannel.getMarketingDetails()
        );
    }
}
