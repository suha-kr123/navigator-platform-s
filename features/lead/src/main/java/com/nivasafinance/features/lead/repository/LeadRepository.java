package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Lead;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface LeadRepository extends JpaRepository<Lead, Long> {

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT * FROM n_lead WHERE preliminary_details->>'phoneNo' = :phoneNo",
        nativeQuery = true
    )
    List<Lead> findByPhoneNo(@org.springframework.data.repository.query.Param("phoneNo") String phoneNo);
}
