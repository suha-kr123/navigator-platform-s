package com.nivasafinance.features.transaction.repository;

import com.nivasafinance.features.transaction.entity.TransactionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionPaymentRepository extends JpaRepository<TransactionPayment, Long> {

    List<TransactionPayment> findByTransactionId(Long transactionId);
}
