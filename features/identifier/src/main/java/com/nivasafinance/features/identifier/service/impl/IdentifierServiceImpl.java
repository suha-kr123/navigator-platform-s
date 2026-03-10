package com.nivasafinance.features.identifier.service.impl;

import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.common.validator.IdentifierValidator;
import com.nivasafinance.common.validator.IdentifierValidatorFactory;
import com.nivasafinance.features.identifier.service.IdentifierService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdentifierServiceImpl implements IdentifierService {

    private final IdentifierValidatorFactory identifierValidatorFactory;

    public IdentifierServiceImpl(IdentifierValidatorFactory identifierValidatorFactory) {
        this.identifierValidatorFactory = identifierValidatorFactory;
    }

    @Override
    public IdentifierData createIdentifierData(IdentifierRequest identifierRequest) {
        IdentifierValidator validator = identifierValidatorFactory.getValidator(identifierRequest.getType());
        if (validator != null) {
            validator.validate(identifierRequest.getIdentifier());
        }
        return IdentifierData
                .builder()
                .id(UUID.randomUUID())
                .type(identifierRequest.getType())
                .identifier(identifierRequest.getIdentifier())
                .build();
    }
}