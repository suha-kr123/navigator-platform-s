package com.nivasafinance.features.leadqueues.service.impl;

import com.nivasafinance.features.leadqueues.entity.QueueConfig;
import com.nivasafinance.features.leadqueues.repository.QueueConfigRepositoryWrapper;
import com.nivasafinance.features.leadqueues.service.QueueReorderService;
import com.nivasafinance.features.leadqueues.service.QueueReorderExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueReorderServiceImpl implements QueueReorderService {

    private final QueueConfigRepositoryWrapper queueConfigRepositoryWrapper;
    private final QueueReorderExecutor queueReorderExecutor;

    @Override
    public void reorderQueue(String queueConfigName) {
        QueueConfig queueConfig = queueConfigRepositoryWrapper
                .findByQueueName(queueConfigName);

        LocalDateTime lastReorderTime = queueConfig.getLastReorderTime();
        // reorder_time in n_queue_config is in seconds (freshness window after last_reorder_time)
        int reorderTimeSeconds = queueConfig.getReorderTime() != null && queueConfig.getReorderTime() > 0
                ? queueConfig.getReorderTime()
                : 5;

        boolean isStale = lastReorderTime == null
                || lastReorderTime.plusSeconds(reorderTimeSeconds).isBefore(LocalDateTime.now());

        if (!isStale) {
            log.debug("Queue {} is fresh, skipping reorder", queueConfigName);
            return;
        }

        queueReorderExecutor.doReorder(queueConfig);
    }
}