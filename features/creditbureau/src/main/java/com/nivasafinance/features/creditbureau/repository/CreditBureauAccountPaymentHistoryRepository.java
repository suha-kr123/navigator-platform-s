package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauAccountPaymentHistoryRepository extends JpaRepository<CreditBureauAccountPaymentHistory, Long> {
    Optional<CreditBureauAccountPaymentHistory> findByIdentifier(UUID identifier);
    List<CreditBureauAccountPaymentHistory> findByTradelineId(Long tradelineId);
    void deleteByTradelineId(Long tradelineId);
}
