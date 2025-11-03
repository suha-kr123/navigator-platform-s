package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreateLeadContactRequest;
import com.nivasafinance.features.lead.dto.UpdateLeadContactRequest;

import java.util.UUID;

public interface LeadContactWriteService {
    void createContact(UUID leadId, CreateLeadContactRequest request);
    void updateContact(UUID leadId, UUID contactId, UpdateLeadContactRequest request);
    void deleteContact(UUID leadId, UUID contactId);
}

