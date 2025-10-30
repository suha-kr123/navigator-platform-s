package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;

public interface LeadWriteService {
    CreateLeadResponse createLead(CreateLeadRequest request);
}
