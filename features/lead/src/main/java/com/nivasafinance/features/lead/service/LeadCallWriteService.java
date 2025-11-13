package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreateLeadCallRequest;
import com.nivasafinance.features.lead.dto.CreateLeadCallResponse;

import java.util.UUID;

public interface LeadCallWriteService {

    CreateLeadCallResponse callContact(UUID leadIdentifier, CreateLeadCallRequest request);
}

