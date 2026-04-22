package com.nivasafinance.features.leadqueues.service;

import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import com.nivasafinance.features.leadqueues.entity.QueueConfig;
import com.nivasafinance.features.leadqueues.repository.LeadQueueRepositoryWrapper;
import com.nivasafinance.features.leadqueues.repository.QueueConfigRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueReorderExecutor {

    private final DataProviderExecutor dataProviderExecutor;
    private final LeadQueueRepositoryWrapper leadQueueRepositoryWrapper;
    private final QueueConfigRepositoryWrapper queueConfigRepositoryWrapper;

    @Transactional
    public void doReorder(QueueConfig queueConfig) {

        Long queueConfigId = queueConfig.getId();
        String providerName = queueConfig.getDataProvider().getDataProviderName();

        // 1. Fetch sorted leads
        List<Map<String, Object>> sortedLeads = dataProviderExecutor.executeDataProviderForList(providerName, Map.of());

        if (sortedLeads.isEmpty()) {
            log.info("Provider returned no leads for queue: {}", queueConfig.getQueueName());

            leadQueueRepositoryWrapper.softDeleteAllUnlocked(queueConfigId);
            queueConfigRepositoryWrapper.updateLastReorderTime(queueConfigId);

            return;
        }

        // 2. Convert to (leadId, position), first-wins on duplicate lead_id (aligns with n_lead_queue_temp PK)
        List<LeadQueueRepositoryWrapper.LeadPosition> leadPositions = new ArrayList<>();
        Set<Long> seenLeadIds = new HashSet<>();
        for (int i = 0; i < sortedLeads.size(); i++) {
            Object rawId = sortedLeads.get(i).get("lead_id");
            if (rawId == null) {
                log.warn("Data provider row {} omitted: missing lead_id for queue {}", i, queueConfig.getQueueName());
                continue;
            }
            Long leadId = toLong(rawId);
            if (!seenLeadIds.add(leadId)) {
                log.warn(
                        "Data provider row {}: duplicate lead_id {} skipped (first occurrence wins) for queue {}",
                        i,
                        leadId,
                        queueConfig.getQueueName());
                continue;
            }
            leadPositions.add(new LeadQueueRepositoryWrapper.LeadPosition(leadId, leadPositions.size() + 1));
        }

        if (leadPositions.isEmpty()) {
            log.info("No valid lead ids after data provider for queue: {}", queueConfig.getQueueName());
            leadQueueRepositoryWrapper.softDeleteAllUnlocked(queueConfigId);
            queueConfigRepositoryWrapper.updateLastReorderTime(queueConfigId);
            return;
        }

        // 3. Push to temp table
        leadQueueRepositoryWrapper.bulkInsertTempLeadPositions(queueConfigId, leadPositions);

        // 4. DB operations (active position updates, then reactivate soft-deleted, then new rows, then remove stragglers)
        int updated = leadQueueRepositoryWrapper.updatePositionsFromTemp(queueConfigId);
        int reactivated = leadQueueRepositoryWrapper.reactivateInactiveFromTemp(queueConfigId);
        int inserted = leadQueueRepositoryWrapper.insertNewFromTemp(queueConfigId);
        int deleted = leadQueueRepositoryWrapper.softDeleteMissingFromTemp(queueConfigId);

        // 5. Update metadata
        queueConfigRepositoryWrapper.updateLastReorderTime(queueConfigId);

        log.info(
                "Reordered queue {} → total: {}, position updates: {}, reactivated: {}, inserted: {}, removed: {}",
                queueConfig.getQueueName(),
                leadPositions.size(),
                updated,
                reactivated,
                inserted,
                deleted);
    }

    private Long toLong(Object value) {
        if (value instanceof Number)
            return ((Number) value).longValue();
        if (value instanceof String)
            return Long.parseLong((String) value);
        throw new IllegalArgumentException("Cannot convert to Long: " + value);
    }
}