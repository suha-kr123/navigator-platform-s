package com.nivasafinance.features.leadqueues.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.nivasafinance.features.leadqueues.entity.QueueConfig;
import com.nivasafinance.features.leadqueues.exception.QueueConfigNotFoundException;
import com.nivasafinance.features.leadqueues.exception.QueueConfigOperationException;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueueConfigRepositoryWrapper {

    private final JdbcTemplate jdbcTemplate;
    private final QueueConfigRepository queueConfigRepository;
    private final MessageSource messageSource;  

    public QueueConfig findByQueueName(String queueConfigName) {

        Optional<QueueConfig> queueConfig = queueConfigRepository.findByQueueNameIsActiveTrue(queueConfigName);
        if (queueConfig.isEmpty()) {
            throw QueueConfigNotFoundException.notFoundByName(queueConfigName, messageSource);
        }
        return queueConfig.get();
    }

    public List<QueueConfig> findAllActiveByUserId(Long userId) {
        return queueConfigRepository.findAllActiveByUserId(userId);
    }

    public void updateLastReorderTime(Long queueConfigId) {

        int updated = jdbcTemplate.update(
                "UPDATE n_queue_config SET last_reorder_time = NOW() WHERE id = ?",
                queueConfigId);

        if (updated == 0) {
            throw QueueConfigOperationException.updateLastReorderTimeFailed(messageSource);
        }
    }

    public String getQueueNameByIdOrEmpty(Long queueConfigId) {
        return queueConfigRepository
                .findById(queueConfigId)
                .map(QueueConfig::getQueueName)
                .orElse("");
    }
}