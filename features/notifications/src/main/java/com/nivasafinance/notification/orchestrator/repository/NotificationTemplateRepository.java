package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByIdentifier(String identifier);
    
    /**
     * Case-insensitive lookup for template identifier.
     * This helps handle cases where template identifiers might have case mismatches.
     */
    @Query("SELECT t FROM NotificationTemplate t WHERE LOWER(t.identifier) = LOWER(:identifier)")
    Optional<NotificationTemplate> findByIdentifierIgnoreCase(@Param("identifier") String identifier);
}

