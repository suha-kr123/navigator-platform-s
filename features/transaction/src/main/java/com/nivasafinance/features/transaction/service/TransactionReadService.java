package com.nivasafinance.features.transaction.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.transaction.dto.DisbursedLeadResponse;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import com.nivasafinance.features.transaction.dto.TransactionDashboardFilters;
import com.nivasafinance.features.transaction.dto.TransactionDetailResponse;
import com.nivasafinance.features.transaction.dto.TransactionEventResponse;
import com.nivasafinance.features.transaction.dto.TransactionSearchResponse;

import java.util.List;
import java.util.UUID;

public interface TransactionReadService {

    TransactionDetailResponse getByIdentifier(UUID identifier);

    List<TransactionEventResponse> getEventHistory(UUID identifier);

    List<LeadTransactionResponse> getLeadTransactions(UUID leadIdentifier);

    PaginatedResponse<LeadTransactionResponse> getTransactionDashboard(
            PaginationRequest paginationRequest,
            TransactionDashboardFilters filters);

    List<LeadTransactionResponse> getTransactionsByReferralCode(String referralCode);

    TransactionSearchResponse searchByLeadMobileNumber(String mobileNumber);

    TransactionSearchResponse searchByAdvisorMobileNumber(String mobileNumber);

    PaginatedResponse<DisbursedLeadResponse> getDisbursedLeadsPendingTransaction(
            PaginationRequest paginationRequest);
}
