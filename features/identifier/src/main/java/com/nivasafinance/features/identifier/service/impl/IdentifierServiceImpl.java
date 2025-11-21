package com.nivasafinance.features.identifier.service.impl;

import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.features.identifier.service.IdentifierService;
import org.springframework.stereotype.Service;

import java.util.UUID;

// Today This IdentifierServiceImpl is returning IdentifierData. But later it will handle
// validation, verification related to identifier

@Service
public class IdentifierServiceImpl implements IdentifierService {

    @Override
    public IdentifierData createIdentifierData(IdentifierRequest identifierRequest) {
        return IdentifierData
                .builder()
                .id(UUID.randomUUID())
                .type(identifierRequest.getType())
                .identifier(identifierRequest.getIdentifier())
                .build();
    }
}