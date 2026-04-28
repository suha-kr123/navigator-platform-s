package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadTransactionReadService;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import com.nivasafinance.features.transaction.service.TransactionReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class LeadTransactionReadServiceImpl implements LeadTransactionReadService {

    private final TransactionReadService transactionReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;

    @Override
    public List<LeadTransactionResponse> getLeadTransactions(UUID leadIdentifier) {
        leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        return transactionReadService.getLeadTransactions(leadIdentifier);
    }
}
