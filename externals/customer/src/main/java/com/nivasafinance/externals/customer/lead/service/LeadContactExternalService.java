package com.nivasafinance.externals.customer.lead.service;

import com.nivasafinance.common.dto.IdentifierData;

import java.util.Optional;
import java.util.UUID;

public interface LeadContactExternalService {

    IdentifierData addOrReplacePan(UUID leadId, UUID contactIdentifier, String pan);

    Optional<IdentifierData> getPan(UUID leadId, UUID contactIdentifier);
}
