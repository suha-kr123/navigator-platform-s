package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountSecurityDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauAccountSecurityDetailsRepository extends JpaRepository<CreditBureauAccountSecurityDetails, Long> {
    Optional<CreditBureauAccountSecurityDetails> findByIdentifier(UUID identifier);
    List<CreditBureauAccountSecurityDetails> findByTradelineId(Long tradelineId);
    void deleteByTradelineId(Long tradelineId);
}
