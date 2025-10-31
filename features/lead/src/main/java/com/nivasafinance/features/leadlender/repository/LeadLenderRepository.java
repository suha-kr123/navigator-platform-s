package com.nivasafinance.features.leadlender.repository;

import com.nivasafinance.features.leadlender.entity.LeadLender;
import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface LeadLenderRepository extends JpaRepository<LeadLender, Long> {
    
    Optional<LeadLender> findByLenderIdentifier(UUID lenderIdentifier);
    
    List<LeadLender> findByLeadId(Long leadId);
    
    List<LeadLender> findByLenderKey(String lenderKey);
    
    List<LeadLender> findByStatus(LeadLenderStatus status);
    
    Optional<LeadLender> findByLeadIdAndLenderKey(Long leadId, String lenderKey);
    
    List<LeadLender> findByLeadIdAndStatus(Long leadId, LeadLenderStatus status);

    @Query("SELECT ll FROM LeadLender ll WHERE ll.leadId = :leadId AND ll.status = :status ORDER BY ll.createdAt DESC")
    List<LeadLender> findByLeadIdAndStatusOrderByCreatedAtDesc(
        @Param("leadId") Long leadId,
        @Param("status") LeadLenderStatus status
    );
}

