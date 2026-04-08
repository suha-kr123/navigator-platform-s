package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Lead;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface LeadRepository extends JpaRepository<Lead, Long> {

    @Query("SELECT l FROM Lead l WHERE l.leadIdentifier = :leadIdentifier AND l.isDeleted = false")
    Optional<Lead> findByLeadIdentifier(@Param("leadIdentifier") UUID leadIdentifier);

    // Unfiltered — used by admin delete/undo-delete operations
    @Query("SELECT l FROM Lead l WHERE l.leadIdentifier = :leadIdentifier")
    Optional<Lead> findByLeadIdentifierIncludingDeleted(@Param("leadIdentifier") UUID leadIdentifier);
}
