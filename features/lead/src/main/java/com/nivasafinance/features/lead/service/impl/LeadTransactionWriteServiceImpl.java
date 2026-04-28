package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.entity.SourcingChannel;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadTransactionWriteService;
import com.nivasafinance.features.transaction.dto.CreateLeadTransactionRequest;
import com.nivasafinance.features.transaction.dto.CreateLeadTransactionResponse;
import com.nivasafinance.features.transaction.dto.CreateTransactionRequest;
import com.nivasafinance.features.transaction.entity.LeadTransaction;
import com.nivasafinance.features.transaction.entity.Transaction;
import com.nivasafinance.features.transaction.service.TransactionWriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class LeadTransactionWriteServiceImpl implements LeadTransactionWriteService {

    private final TransactionWriteService transactionWriteService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;

    @Override
    public CreateLeadTransactionResponse createLeadTransaction(UUID leadIdentifier, CreateLeadTransactionRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        String referralCode = resolveReferralCode(lead);

        CreateTransactionRequest transactionRequest = CreateTransactionRequest.builder()
                .amount(request.getAmount())
                .idempotencyKey(request.getIdempotencyKey())
                .remarks(request.getRemarks())
                .build();

        Transaction savedTransaction = transactionWriteService.createTransaction(transactionRequest);

        LeadTransaction leadTransaction = new LeadTransaction();
        leadTransaction.setIdentifier(UUID.randomUUID());
        leadTransaction.setLeadId(lead.getId());
        leadTransaction.setTransactionId(savedTransaction.getId());
        leadTransaction.setDomainType(request.getDomainType());
        leadTransaction.setReferralCode(referralCode);
        leadTransaction.setCreatedBy(UserContext.getUsername());
        leadTransaction.setCreatedAt(LocalDateTime.now());

        transactionWriteService.saveLeadTransactionContext(leadTransaction);

        return CreateLeadTransactionResponse.builder()
                .identifier(savedTransaction.getIdentifier())
                .build();
    }

    private String resolveReferralCode(Lead lead) {
        if (lead.getSourcingChannelId() == null) {
            return null;
        }
        try {
            SourcingChannel sourcingChannel = sourcingChannelRepositoryWrapper.findByIdWithException(lead.getSourcingChannelId());
            if (sourcingChannel.getMarketingDetails() == null) {
                return null;
            }
            return sourcingChannel.getMarketingDetails().getReferredByCode();
        } catch (RuntimeException e) {
            log.warn("Failed to resolve referral code for lead id={}: {}", lead.getId(), e.getMessage());
            return null;
        }
    }
}
