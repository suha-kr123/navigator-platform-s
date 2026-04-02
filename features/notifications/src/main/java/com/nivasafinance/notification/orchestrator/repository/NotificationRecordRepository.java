package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.common.enums.NotificationStatus;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, UUID> {

    Optional<NotificationRecord> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT r FROM NotificationRecord r WHERE r.status IN :statuses AND r.createdAt < :cutoff")
    List<NotificationRecord> findStaleByStatusesAndCreatedBefore(
            @Param("statuses") List<NotificationStatus> statuses,
            @Param("cutoff") LocalDateTime cutoff);
}


