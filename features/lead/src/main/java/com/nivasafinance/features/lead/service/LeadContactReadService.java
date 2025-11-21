package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.lead.dto.LeadContactResponse;

import java.util.List;
import java.util.UUID;

public interface LeadContactReadService {
    List<LeadContactResponse> getContacts(UUID leadId);
    LeadContactResponse getContactById(UUID leadId, UUID contactId);

    List<AddressData> getAddresses(UUID identifier);

    AddressData getAddress(UUID identifier, String addressId);

    List<IdentifierData> getIdentifiers(UUID leadId, UUID contactIdentifier);

    IdentifierData getIdentifier(UUID leadId, UUID contactIdentifier, UUID identifierId);
}

