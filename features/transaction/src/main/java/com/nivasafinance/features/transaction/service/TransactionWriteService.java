package com.nivasafinance.features.transaction.service;

import com.nivasafinance.features.transaction.dto.CreateTransactionRequest;
import com.nivasafinance.features.transaction.dto.DeclineTransactionRequest;
import com.nivasafinance.features.transaction.dto.FailTransactionRequest;
import com.nivasafinance.features.transaction.dto.PayTransactionRequest;
import com.nivasafinance.features.transaction.entity.LeadTransaction;
import com.nivasafinance.features.transaction.entity.Transaction;

import java.util.UUID;

public interface TransactionWriteService {

    Transaction createTransaction(CreateTransactionRequest request);

    void saveLeadTransactionContext(LeadTransaction leadTransaction);

    void markPaid(UUID identifier, PayTransactionRequest request);

    void markFailed(UUID identifier, FailTransactionRequest request);

    void declineTransaction(UUID identifier, DeclineTransactionRequest request);
}
