package com.nivasafinance.features.transaction.service.impl;

import com.nivasafinance.features.transaction.dto.CreateTransactionRequest;
import com.nivasafinance.features.transaction.dto.DeclineTransactionRequest;
import com.nivasafinance.features.transaction.dto.FailTransactionRequest;
import com.nivasafinance.features.transaction.dto.PayTransactionRequest;
import com.nivasafinance.features.transaction.entity.LeadTransaction;
import com.nivasafinance.features.transaction.entity.Transaction;
import com.nivasafinance.features.transaction.entity.TransactionEvent;
import com.nivasafinance.features.transaction.entity.TransactionPayment;
import com.nivasafinance.features.transaction.enums.PaymentStatus;
import com.nivasafinance.features.transaction.enums.TransactionStatus;
import com.nivasafinance.features.transaction.exception.TransactionExceptionFactory;
import com.nivasafinance.features.transaction.repository.TransactionRepositoryWrapper;
import com.nivasafinance.features.transaction.service.TransactionWriteService;
import com.nivasafinance.common.context.UserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class TransactionWriteServiceImpl implements TransactionWriteService {

    private static final String LOG_TRANSACTION_CREATED = "Transaction created: {}";
    private static final String LOG_TRANSACTION_STATUS_CHANGED = "Transaction {} status changed to {}";

    private final TransactionRepositoryWrapper repositoryWrapper;
    private final MessageSource messageSource;

    @Override
    public Transaction createTransaction(CreateTransactionRequest request) {
        if (repositoryWrapper.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw TransactionExceptionFactory.duplicateTransaction(request.getIdempotencyKey(), messageSource);
        }

        String actor = getCurrentUsername();

        Transaction transaction = new Transaction();
        transaction.setIdentifier(UUID.randomUUID());
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.CREATED);
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setRemarks(new ArrayList<>());
        appendRemark(transaction, request.getRemarks(), actor);

        Transaction savedTransaction = repositoryWrapper.saveTransaction(transaction);

        TransactionEvent event = buildEvent(
                savedTransaction.getId(),
                TransactionStatus.CREATED,
                actor,
                request.getRemarks(),
                null,
                null
        );
        repositoryWrapper.saveEvent(event);

        log.info(LOG_TRANSACTION_CREATED, savedTransaction.getIdentifier());
        return savedTransaction;
    }

    @Override
    public void saveLeadTransactionContext(LeadTransaction leadTransaction) {
        repositoryWrapper.saveLeadTransaction(leadTransaction);
    }

    @Override
    public void markPaid(UUID identifier, PayTransactionRequest request) {
        Transaction transaction = repositoryWrapper.findByIdentifier(identifier);
        validateStatusTransition(transaction, TransactionStatus.PAID, Set.of(TransactionStatus.CREATED, TransactionStatus.FAILED));

        String actor = getCurrentUsername();

        TransactionPayment payment = new TransactionPayment();
        payment.setIdentifier(UUID.randomUUID());
        payment.setTransactionId(transaction.getId());
        payment.setPaymentMode(request.getPaymentMode());
        payment.setExternalReference(request.getExternalReference());
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        payment.setRecordedBy(actor);
        payment.setPaymentData(request.getPaymentData());
        payment.setCreatedAt(LocalDateTime.now());

        TransactionPayment savedPayment = repositoryWrapper.savePayment(payment);

        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("paymentMode", request.getPaymentMode().name());
        paymentDetails.put("externalReference", request.getExternalReference());
        if (request.getPaymentData() != null) {
            paymentDetails.putAll(request.getPaymentData());
        }

        TransactionEvent event = buildEvent(
                transaction.getId(),
                TransactionStatus.PAID,
                actor,
                request.getRemarks(),
                paymentDetails,
                savedPayment.getId()
        );
        repositoryWrapper.saveEvent(event);

        appendRemark(transaction, request.getRemarks(), actor);
        transaction.setStatus(TransactionStatus.PAID);
        repositoryWrapper.saveTransaction(transaction);

        log.info(LOG_TRANSACTION_STATUS_CHANGED, identifier, TransactionStatus.PAID);
    }

    @Override
    public void markFailed(UUID identifier, FailTransactionRequest request) {
        Transaction transaction = repositoryWrapper.findByIdentifier(identifier);
        validateStatusTransition(transaction, TransactionStatus.FAILED, Set.of(TransactionStatus.CREATED));

        String actor = getCurrentUsername();

        Long paymentId = null;
        if (request.getPaymentMode() != null) {
            TransactionPayment payment = new TransactionPayment();
            payment.setIdentifier(UUID.randomUUID());
            payment.setTransactionId(transaction.getId());
            payment.setPaymentMode(request.getPaymentMode());
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setRecordedBy(actor);
            payment.setPaymentData(request.getPaymentData());
            payment.setCreatedAt(LocalDateTime.now());

            TransactionPayment savedPayment = repositoryWrapper.savePayment(payment);
            paymentId = savedPayment.getId();
        }

        TransactionEvent event = buildEvent(
                transaction.getId(),
                TransactionStatus.FAILED,
                actor,
                request.getRemarks(),
                null,
                paymentId
        );
        repositoryWrapper.saveEvent(event);

        appendRemark(transaction, request.getRemarks(), actor);
        transaction.setStatus(TransactionStatus.FAILED);
        repositoryWrapper.saveTransaction(transaction);

        log.info(LOG_TRANSACTION_STATUS_CHANGED, identifier, TransactionStatus.FAILED);
    }

    @Override
    public void declineTransaction(UUID identifier, DeclineTransactionRequest request) {
        Transaction transaction = repositoryWrapper.findByIdentifier(identifier);
        validateStatusTransition(transaction, TransactionStatus.DECLINED, Set.of(TransactionStatus.CREATED, TransactionStatus.FAILED));

        String actor = getCurrentUsername();

        TransactionEvent event = buildEvent(
                transaction.getId(),
                TransactionStatus.DECLINED,
                actor,
                request.getRemarks(),
                null,
                null
        );
        repositoryWrapper.saveEvent(event);

        appendRemark(transaction, request.getRemarks(), actor);
        transaction.setStatus(TransactionStatus.DECLINED);
        repositoryWrapper.saveTransaction(transaction);

        log.info(LOG_TRANSACTION_STATUS_CHANGED, identifier, TransactionStatus.DECLINED);
    }

    private void validateStatusTransition(Transaction transaction, TransactionStatus target, Set<TransactionStatus> allowedFrom) {
        if (!allowedFrom.contains(transaction.getStatus())) {
            throw TransactionExceptionFactory.invalidStatusTransition(
                    transaction.getStatus().name(),
                    target.name(),
                    messageSource
            );
        }
    }

    private TransactionEvent buildEvent(Long transactionId, TransactionStatus eventType,
                                        String actor, String remarks,
                                        Map<String, Object> paymentDetails, Long paymentId) {
        TransactionEvent event = new TransactionEvent();
        event.setIdentifier(UUID.randomUUID());
        event.setTransactionId(transactionId);
        event.setEventType(eventType);
        event.setActorUsername(actor);
        event.setRemarks(remarks);
        event.setPaymentDetails(paymentDetails);
        event.setEventTimestamp(LocalDateTime.now());
        event.setTransactionPaymentId(paymentId);
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }

    private void appendRemark(Transaction transaction, String remarkText, String actor) {
        if (remarkText == null || remarkText.isBlank()) {
            return;
        }
        List<Map<String, Object>> remarks = transaction.getRemarks();
        if (remarks == null) {
            remarks = new ArrayList<>();
            transaction.setRemarks(remarks);
        }
        Map<String, Object> entry = new HashMap<>();
        entry.put("text", remarkText);
        entry.put("actor", actor);
        entry.put("at", LocalDateTime.now().toString());
        remarks.add(entry);
    }

    private String getCurrentUsername() {
        return UserContext.getUsername();
    }
}
