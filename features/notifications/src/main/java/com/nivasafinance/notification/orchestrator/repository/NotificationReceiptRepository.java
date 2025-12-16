package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationReceiptRepository extends JpaRepository<NotificationReceipt, UUID> {
}

