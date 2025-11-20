package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.lead.dto.CreateLeadContactRequest;
import com.nivasafinance.features.lead.dto.UpdateLeadContactRequest;
import jakarta.validation.Valid;

import java.util.UUID;

public interface LeadContactWriteService {
    void createContact(UUID leadId, CreateLeadContactRequest request);
    void updateContact(UUID leadId, UUID contactId, UpdateLeadContactRequest request);
    void deleteContact(UUID leadId, UUID contactId);

    String addAddress(UUID contactIdentifier, @Valid AddressRequest request);

    void updateAddress(UUID contactIdentifier, String addressId, @Valid AddressRequest request);
}

