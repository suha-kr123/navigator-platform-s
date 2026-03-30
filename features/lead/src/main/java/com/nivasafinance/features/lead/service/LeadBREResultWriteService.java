package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;

import java.util.UUID;

public interface LeadBREResultWriteService {

    LeadBREResultExecuteResponse executeBre(UUID leadId, String config);
}
