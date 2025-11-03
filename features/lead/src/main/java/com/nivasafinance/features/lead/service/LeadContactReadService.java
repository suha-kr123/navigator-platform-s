package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadContactResponse;

import java.util.List;
import java.util.UUID;

public interface LeadContactReadService {
    List<LeadContactResponse> getContacts(UUID leadId);
    LeadContactResponse getContactById(UUID leadId, UUID contactId);
}

