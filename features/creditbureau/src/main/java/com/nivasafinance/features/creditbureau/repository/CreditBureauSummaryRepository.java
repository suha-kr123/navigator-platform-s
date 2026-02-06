package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauSummaryRepository extends JpaRepository<CreditBureauSummary, Long> {
    Optional<CreditBureauSummary> findByIdentifier(UUID identifier);
    Optional<CreditBureauSummary> findByEnquiryId(Long enquiryId);
}
