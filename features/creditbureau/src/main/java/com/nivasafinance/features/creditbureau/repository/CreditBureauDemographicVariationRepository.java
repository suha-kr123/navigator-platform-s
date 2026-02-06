package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauDemographicVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauDemographicVariationRepository extends JpaRepository<CreditBureauDemographicVariation, Long> {
    Optional<CreditBureauDemographicVariation> findByIdentifier(UUID identifier);
    List<CreditBureauDemographicVariation> findByEnquiryId(Long enquiryId);
    void deleteByEnquiryId(Long enquiryId);
}
