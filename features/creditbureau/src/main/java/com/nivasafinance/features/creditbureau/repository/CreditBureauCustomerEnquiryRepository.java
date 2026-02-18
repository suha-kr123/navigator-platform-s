package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauCustomerEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauCustomerEnquiryRepository extends JpaRepository<CreditBureauCustomerEnquiry, Long> {
    Optional<CreditBureauCustomerEnquiry> findByIdentifier(UUID identifier);
    List<CreditBureauCustomerEnquiry> findByEnquiryId(Long enquiryId);
    void deleteByEnquiryId(Long enquiryId);
}
