package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauTradeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauTradelineRepository extends JpaRepository<CreditBureauTradeline, Long> {
    Optional<CreditBureauTradeline> findByIdentifier(UUID identifier);
    List<CreditBureauTradeline> findByEnquiryId(Long enquiryId);
    void deleteByEnquiryId(Long enquiryId);
}
