package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationReceiptRepository extends JpaRepository<NotificationReceipt, UUID> {

    @Query("SELECT r FROM NotificationReceipt r WHERE r.status IN :statuses AND r.createdAt < :cutoff")
    List<NotificationReceipt> findStaleByStatusesAndCreatedBefore(
            @Param("statuses") List<NotificationStatus> statuses,
            @Param("cutoff") LocalDateTime cutoff);
}

