package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauTrends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauTrendsRepository extends JpaRepository<CreditBureauTrends, Long> {
    Optional<CreditBureauTrends> findByIdentifier(UUID identifier);
    List<CreditBureauTrends> findByEnquiryId(Long enquiryId);
    List<CreditBureauTrends> findByEnquiryIdOrderByDateDesc(Long enquiryId);
}