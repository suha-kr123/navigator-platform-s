package com.nivasafinance.features.transaction.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.transaction.dto.DisbursedLeadResponse;
import com.nivasafinance.features.transaction.dto.LeadTransactionContext;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import com.nivasafinance.features.transaction.dto.TransactionDashboardFilters;
import com.nivasafinance.features.transaction.dto.TransactionDetailResponse;
import com.nivasafinance.features.transaction.dto.TransactionEventResponse;
import com.nivasafinance.features.transaction.dto.TransactionPaymentResponse;
import com.nivasafinance.features.transaction.dto.TransactionSearchResponse;
import com.nivasafinance.features.transaction.entity.LeadTransaction;
import com.nivasafinance.features.transaction.entity.Transaction;
import com.nivasafinance.features.transaction.entity.TransactionEvent;
import com.nivasafinance.features.transaction.entity.TransactionPayment;
import com.nivasafinance.features.transaction.repository.TransactionRepositoryWrapper;
import com.nivasafinance.features.transaction.service.TransactionReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionReadServiceImpl implements TransactionReadService {

    private final TransactionRepositoryWrapper repositoryWrapper;

    @Override
    public TransactionDetailResponse getByIdentifier(UUID identifier) {
        Transaction transaction = repositoryWrapper.findByIdentifier(identifier);
        Long transactionId = transaction.getId();
        List<TransactionEvent> events = repositoryWrapper.getEventHistory(transactionId);
        List<TransactionPayment> payments = repositoryWrapper.getPayments(transactionId);
        LeadTransaction leadTransaction = repositoryWrapper.findLeadTransactionByTransaction(transactionId)
                .orElse(null);

        return TransactionDetailResponse.builder()
                .identifier(transaction.getIdentifier())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .idempotencyKey(transaction.getIdempotencyKey())
                .createdBy(transaction.getCreatedBy())
                .createdAt(transaction.getCreatedAt())
                .remarks(transaction.getRemarks())
                .events(mapEvents(events))
                .payments(mapPayments(payments))
                .leadContext(mapLeadContext(leadTransaction))
                .build();
    }

    @Override
    public List<TransactionEventResponse> getEventHistory(UUID identifier) {
        Transaction transaction = repositoryWrapper.findByIdentifier(identifier);
        List<TransactionEvent> events = repositoryWrapper.getEventHistory(transaction.getId());
        return mapEvents(events);
    }

    @Override
    public List<LeadTransactionResponse> getLeadTransactions(UUID leadIdentifier) {
        return repositoryWrapper.searchByLeadIdentifier(leadIdentifier);
    }

    @Override
    public PaginatedResponse<LeadTransactionResponse> getTransactionDashboard(
            PaginationRequest paginationRequest,
            TransactionDashboardFilters filters) {
        return repositoryWrapper.getTransactionDashboard(paginationRequest, filters);
    }

    @Override
    public List<LeadTransactionResponse> getTransactionsByReferralCode(String referralCode) {
        return repositoryWrapper.searchByReferralCode(referralCode);
    }

    @Override
    public TransactionSearchResponse searchByLeadMobileNumber(String mobileNumber) {
        return TransactionSearchResponse.builder()
                .transactions(repositoryWrapper.searchByLeadMobileNumber(mobileNumber))
                .pendingLeads(repositoryWrapper.searchDisbursedLeadsByLeadMobile(mobileNumber))
                .build();
    }

    @Override
    public TransactionSearchResponse searchByAdvisorMobileNumber(String mobileNumber) {
        return TransactionSearchResponse.builder()
                .transactions(repositoryWrapper.searchByAdvisorMobileNumber(mobileNumber))
                .pendingLeads(repositoryWrapper.searchDisbursedLeadsByAdvisorMobile(mobileNumber))
                .build();
    }

    @Override
    public PaginatedResponse<DisbursedLeadResponse> getDisbursedLeadsPendingTransaction(
            PaginationRequest paginationRequest) {
        return repositoryWrapper.getDisbursedLeadsPendingTransaction(paginationRequest);
    }

    private List<TransactionEventResponse> mapEvents(List<TransactionEvent> events) {
        return events.stream()
                .map(event -> TransactionEventResponse.builder()
                        .identifier(event.getIdentifier())
                        .eventType(event.getEventType().name())
                        .actorUsername(event.getActorUsername())
                        .remarks(event.getRemarks())
                        .paymentDetails(event.getPaymentDetails())
                        .eventTimestamp(event.getEventTimestamp())
                        .build())
                .toList();
    }

    private List<TransactionPaymentResponse> mapPayments(List<TransactionPayment> payments) {
        return payments.stream()
                .map(payment -> TransactionPaymentResponse.builder()
                        .identifier(payment.getIdentifier())
                        .paymentMode(payment.getPaymentMode().name())
                        .externalReference(payment.getExternalReference())
                        .paymentStatus(payment.getPaymentStatus().name())
                        .paymentDate(payment.getPaymentDate())
                        .recordedBy(payment.getRecordedBy())
                        .paymentData(payment.getPaymentData())
                        .createdAt(payment.getCreatedAt())
                        .build())
                .toList();
    }

    private LeadTransactionContext mapLeadContext(LeadTransaction lt) {
        if (lt == null) {
            return null;
        }
        UUID leadIdentifier = repositoryWrapper.findLeadIdentifierByLeadId(lt.getLeadId());
        return LeadTransactionContext.builder()
                .identifier(lt.getIdentifier())
                .leadIdentifier(leadIdentifier)
                .domainType(lt.getDomainType().name())
                .referralCode(lt.getReferralCode())
                .build();
    }
}
