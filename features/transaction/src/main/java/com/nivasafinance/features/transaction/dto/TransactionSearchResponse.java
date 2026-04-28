package com.nivasafinance.features.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSearchResponse {

    private List<LeadTransactionResponse> transactions;
    private List<DisbursedLeadResponse> pendingLeads;
}
