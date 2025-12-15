package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByIdentifier(String identifier);
}

