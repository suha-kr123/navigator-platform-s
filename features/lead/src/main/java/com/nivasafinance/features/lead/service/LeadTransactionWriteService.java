package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.transaction.dto.CreateLeadTransactionRequest;
import com.nivasafinance.features.transaction.dto.CreateLeadTransactionResponse;

import java.util.UUID;

public interface LeadTransactionWriteService {

    CreateLeadTransactionResponse createLeadTransaction(UUID leadIdentifier, CreateLeadTransactionRequest request);
}
