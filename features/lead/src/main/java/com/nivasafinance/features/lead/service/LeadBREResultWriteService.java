package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.bre.dto.BREExecutionResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LeadBREResultWriteService {

    CompletableFuture<BREExecutionResponse> executeBre(UUID leadId, String config, UUID identifier);
}
