package com.nivasafinance.features.transaction.repository;

import com.nivasafinance.features.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdentifier(UUID identifier);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
