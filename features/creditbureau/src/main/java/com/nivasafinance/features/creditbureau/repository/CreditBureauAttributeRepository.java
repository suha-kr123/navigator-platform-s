package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauAttribute;
import com.nivasafinance.features.creditbureau.enums.CbAttributeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditBureauAttributeRepository extends JpaRepository<CreditBureauAttribute, Long> {

    List<CreditBureauAttribute> findByEnquiryId(Long enquiryId);

    List<CreditBureauAttribute> findByEnquiryIdAndCategory(Long enquiryId, CbAttributeCategory category);

    void deleteByEnquiryId(Long enquiryId);
}
