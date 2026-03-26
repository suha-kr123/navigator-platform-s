package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauDerivedAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditBureauDerivedAttributeRepository extends JpaRepository<CreditBureauDerivedAttribute, Long> {

    void deleteByEnquiryId(Long enquiryId);
}
