package com.nivasafinance.notification.executor.repository;

import com.nivasafinance.notification.executor.entity.NotificationReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationReceiptRepository extends JpaRepository<NotificationReceipt, UUID> {
}


