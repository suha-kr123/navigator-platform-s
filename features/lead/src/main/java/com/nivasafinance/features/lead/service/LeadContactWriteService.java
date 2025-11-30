package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.features.lead.dto.BulkContactsUpdateRequest;
import com.nivasafinance.features.lead.dto.BulkContactsUpdateResponse;
import com.nivasafinance.features.lead.dto.CreateLeadContactRequest;
import com.nivasafinance.features.lead.dto.UpdateLeadContactRequest;
import jakarta.validation.Valid;

import java.util.UUID;

public interface LeadContactWriteService {
    void createContact(UUID leadId, CreateLeadContactRequest request);
    void updateContact(UUID leadId, UUID contactId, UpdateLeadContactRequest request);
    void deleteContact(UUID leadId, UUID contactId);
    
    BulkContactsUpdateResponse bulkUpdateContacts(UUID leadId, BulkContactsUpdateRequest request);

    String addAddress(UUID contactIdentifier, @Valid AddressRequest request);

    void updateAddress(UUID contactIdentifier, String addressId, @Valid AddressRequest request);

    IdentifierData addIdentifier(UUID leadId, UUID contactIdentifier, @Valid IdentifierRequest request);

    void updateIdentifier(UUID leadId, UUID contactIdentifier, UUID identifierId, @Valid IdentifierRequest request);

    void deleteIdentifier(UUID leadId, UUID contactIdentifier, UUID identifierId);
}

