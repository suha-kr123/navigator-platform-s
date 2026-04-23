package com.nivasafinance.externals.customer.lead.service;

import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.lead.dto.LeadContactResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadContactExternalService {

    IdentifierData addOrReplacePan(UUID leadId, UUID contactIdentifier, String pan);

    List<LeadContactResponse> getContacts(UUID leadIdentifier);

    Optional<IdentifierData> getPan(UUID leadId, UUID contactIdentifier);
}
