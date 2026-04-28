package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;

import java.util.List;
import java.util.UUID;

public interface LeadTransactionReadService {

    List<LeadTransactionResponse> getLeadTransactions(UUID leadIdentifier);
}
