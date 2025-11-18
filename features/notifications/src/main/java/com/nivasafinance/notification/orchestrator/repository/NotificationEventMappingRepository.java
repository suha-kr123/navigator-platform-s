package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.NotificationEventMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationEventMappingRepository extends JpaRepository<NotificationEventMapping, Long> {

    List<NotificationEventMapping> findByEventAndStatus(String event, String status);

    @Query("SELECT nem FROM NotificationEventMapping nem " +
            "JOIN FETCH nem.notificationConfig config " +
            "WHERE nem.event = :event AND nem.status = :status")
    List<NotificationEventMapping> findByEventAndStatusWithConfig(@Param("event") String event,
                                                                  @Param("status") String status);
}


