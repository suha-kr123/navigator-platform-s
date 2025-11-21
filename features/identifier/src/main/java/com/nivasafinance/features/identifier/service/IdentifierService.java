package com.nivasafinance.features.identifier.service;

import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;


public interface IdentifierService {

    IdentifierData createIdentifierData(IdentifierRequest identifierRequest);
}