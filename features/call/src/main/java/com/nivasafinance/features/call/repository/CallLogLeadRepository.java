package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLogLead;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@JaversSpringDataAuditable
public interface CallLogLeadRepository extends JpaRepository<CallLogLead, Long> {
    Page<CallLogLead> findByLeadIdOrderByCallLogIdDesc(Long leadId, Pageable pageable);

    List<CallLogLead> findAllByLeadIdOrderByCallLogIdDesc(Long leadId);
}
