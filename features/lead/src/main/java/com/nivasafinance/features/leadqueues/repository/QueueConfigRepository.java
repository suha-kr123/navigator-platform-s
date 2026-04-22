package com.nivasafinance.features.leadqueues.repository;

import com.nivasafinance.features.leadqueues.entity.QueueConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QueueConfigRepository extends JpaRepository<QueueConfig, Long> {

    @Query("SELECT q FROM QueueConfig q WHERE q.queueName = :queueName AND q.isActive = true")
    Optional<QueueConfig> findByQueueNameIsActiveTrue(@Param("queueName") String queueName);

    @Query(value = """
            SELECT id, queue_name, description, data_providers, user_ids, lock_duration, reorder_time, last_reorder_time, is_active, created_by, created_at, updated_by, updated_at, version
            FROM n_queue_config
            WHERE is_active = true
              AND user_ids::jsonb @> to_jsonb(CAST(:userId AS bigint))
            """, nativeQuery = true)
    List<QueueConfig> findAllActiveByUserId(@Param("userId") Long userId);
}
