package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLogLead;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@JaversSpringDataAuditable
public interface CallLogLeadRepository extends JpaRepository<CallLogLead, Long> {
    Page<CallLogLead> findByLeadIdOrderByCallLogIdDesc(Long leadId, Pageable pageable);

    List<CallLogLead> findAllByLeadIdOrderByCallLogIdDesc(Long leadId);

    @Query(value = "SELECT cll.* FROM n_call_log_lead cll " +
            "JOIN n_call_log cl ON cll.call_log_id = cl.id " +
            "WHERE cll.lead_id = :leadId AND cl.ai_analysis IS NOT NULL " +
            "ORDER BY cll.call_log_id DESC",
            countQuery = "SELECT COUNT(*) FROM n_call_log_lead cll " +
                    "JOIN n_call_log cl ON cll.call_log_id = cl.id " +
                    "WHERE cll.lead_id = :leadId AND cl.ai_analysis IS NOT NULL",
            nativeQuery = true)
    Page<CallLogLead> findByLeadIdWithAiAnalysis(@Param("leadId") Long leadId, Pageable pageable);
}
