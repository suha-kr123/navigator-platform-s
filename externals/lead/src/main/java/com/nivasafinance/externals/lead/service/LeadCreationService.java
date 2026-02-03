package com.nivasafinance.externals.lead.service;

import com.nivasafinance.externals.lead.dto.CreateLeadRequest;
import com.nivasafinance.externals.lead.dto.CreateLeadResponse;

public interface LeadCreationService {
    CreateLeadResponse createLead(CreateLeadRequest request);
}
