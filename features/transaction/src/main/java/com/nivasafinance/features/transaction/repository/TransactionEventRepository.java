package com.nivasafinance.features.transaction.repository;

import com.nivasafinance.features.transaction.entity.TransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEventRepository extends JpaRepository<TransactionEvent, Long> {

    List<TransactionEvent> findByTransactionIdOrderByEventTimestampAsc(Long transactionId);
}
