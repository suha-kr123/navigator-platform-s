package com.nivasafinance.features.leadactivity.service;

import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityRequest;
import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityResponse;

public interface LeadActivityWriteService {
    CreateLeadActivityResponse createLeadActivity(CreateLeadActivityRequest request);
}



