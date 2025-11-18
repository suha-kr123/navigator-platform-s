package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.NotificationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, Long> {

    Optional<NotificationConfig> findByIdentifier(UUID identifier);
}


