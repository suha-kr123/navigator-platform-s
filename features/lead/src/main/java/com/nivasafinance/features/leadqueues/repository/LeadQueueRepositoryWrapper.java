package com.nivasafinance.features.leadqueues.repository;

import com.nivasafinance.features.leadqueues.entity.LeadQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadQueueRepositoryWrapper {

    private final JdbcTemplate jdbcTemplate;
    private final LeadQueueRepository leadQueueRepository;

    // DTO
    @Getter
    @AllArgsConstructor
    public static class LeadPosition {
        private Long leadId;
        private Integer position;
    }

    // 1. Insert temp data
    public void bulkInsertTempLeadPositions(Long queueConfigId, List<LeadPosition> leadPositions) {

        // clear previous run for this queue
        jdbcTemplate.update(
                "DELETE FROM n_lead_queue_temp WHERE queue_config_id = ?",
                queueConfigId
        );

        String sql = "INSERT INTO n_lead_queue_temp (queue_config_id, lead_id, position) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                leadPositions,
                1000,
                (ps, lp) -> {
                    ps.setLong(1, queueConfigId);
                    ps.setLong(2, lp.getLeadId());
                    ps.setInt(3, lp.getPosition());
                }
        );
    }

    // 2. Update existing
    public int updatePositionsFromTemp(Long queueConfigId) {
        return jdbcTemplate.update(
                """
                UPDATE n_lead_queue lq
                SET position = t.position,
                    calculated_at = NOW()
                FROM n_lead_queue_temp t
                WHERE lq.lead_id = t.lead_id
                  AND lq.queue_config_id = t.queue_config_id
                  AND lq.queue_config_id = ?
                  AND lq.is_active = true
                  AND NOT (
                        lq.currently_claimed_by IS NOT NULL
                    AND lq.claim_expiry_at > NOW()
                  )
                """,
                queueConfigId
        );
    }

    /**
     * Re-enable rows that were soft-deleted (is_active = false) but appear again in the provider
     * output. Clears any stale claim; position comes from the temp run.
     */
    public int reactivateInactiveFromTemp(Long queueConfigId) {
        return jdbcTemplate.update(
                """
                UPDATE n_lead_queue lq
                SET is_active = true,
                    position = t.position,
                    calculated_at = NOW(),
                    currently_claimed_by = NULL,
                    claim_expiry_at = NULL
                FROM n_lead_queue_temp t
                WHERE lq.lead_id = t.lead_id
                  AND lq.queue_config_id = t.queue_config_id
                  AND lq.queue_config_id = ?
                  AND lq.is_active = false
                """,
                queueConfigId
        );
    }

    /**
     * Insert a row only when no n_lead_queue record exists for this (queue, lead) yet.
     * (Pairs that only had an inactive row are handled by {@link #reactivateInactiveFromTemp}
     * first; without that, a second insert would create duplicates — there is no unique constraint.)
     */
    public int insertNewFromTemp(Long queueConfigId) {
        return jdbcTemplate.update(
                """
                INSERT INTO n_lead_queue (queue_config_id, lead_id, position, calculated_at, is_active)
                SELECT t.queue_config_id, t.lead_id, t.position, NOW(), true
                FROM n_lead_queue_temp t
                WHERE t.queue_config_id = ?
                  AND NOT EXISTS (
                        SELECT 1
                        FROM n_lead_queue lq
                        WHERE lq.queue_config_id = t.queue_config_id
                          AND lq.lead_id = t.lead_id
                  )
                """,
                queueConfigId
        );
    }

    // 4. Soft delete missing
    public int softDeleteMissingFromTemp(Long queueConfigId) {
        return jdbcTemplate.update(
                """
                UPDATE n_lead_queue lq
                SET is_active = false
                WHERE lq.queue_config_id = ?
                  AND lq.is_active = true
                  AND NOT EXISTS (
                        SELECT 1
                        FROM n_lead_queue_temp t
                        WHERE t.lead_id = lq.lead_id
                          AND t.queue_config_id = lq.queue_config_id
                  )
                  AND NOT (
                        lq.currently_claimed_by IS NOT NULL
                    AND lq.claim_expiry_at > NOW()
                  )
                """,
                queueConfigId
        );
    }

    // 5. Soft delete all (empty provider case)
    public void softDeleteAllUnlocked(Long queueConfigId) {
        jdbcTemplate.update(
                """
                UPDATE n_lead_queue
                SET is_active = false
                WHERE queue_config_id = ?
                  AND is_active = true
                  AND (
                        currently_claimed_by IS NULL
                     OR claim_expiry_at IS NULL
                     OR claim_expiry_at <= NOW()
                  )
                """,
                queueConfigId
        );
    }

    public long countActiveByQueueId(Long queueConfigId) {
        return leadQueueRepository.countByQueueConfigIdAndIsActiveTrue(queueConfigId);
    }

    public long countListableByQueueIdForUser(Long queueConfigId, String username, LocalDateTime now) {
        return leadQueueRepository.countListableByQueueIdForUser(queueConfigId, username, now);
    }

    public List<LeadQueue> findPageByQueueId(Long queueConfigId, int limit, int offset) {
        return leadQueueRepository.findPageByQueueId(queueConfigId, limit, offset);
    }

    public List<LeadQueue> findPageByQueueIdForUser(
            Long queueConfigId, String username, LocalDateTime now, int limit, int offset) {
        return leadQueueRepository.findPageByQueueIdForUser(queueConfigId, username, now, limit, offset);
    }

    public Optional<LeadQueue> findByQueueConfigIdAndLeadIdAndIsActiveTrue(Long queueConfigId, Long leadId) {
        return leadQueueRepository.findByQueueConfigIdAndLeadIdAndIsActiveTrue(queueConfigId, leadId);
    }

    public List<LeadQueue> findByLeadIdAndIsActiveTrueOrderByPosition(Long leadId) {
        return leadQueueRepository.findByLeadIdAndIsActiveTrueOrderByPositionAscIdAsc(leadId);
    }

    public Optional<LeadQueue> findFirstClaimableByQueueId(Long queueConfigId, String username, LocalDateTime now) {
        return leadQueueRepository.findFirstClaimableByQueueId(queueConfigId, username, now);
    }

    public Optional<LeadQueue> findByIdAndIsActiveForUpdate(Long id) {
        return leadQueueRepository.findByIdAndIsActiveForUpdate(id);
    }

    public Optional<Long> findFirstClaimableIdForUpdateSkipLocked(
            Long queueConfigId, String username, LocalDateTime now) {
        return leadQueueRepository.findFirstClaimableIdForUpdateSkipLocked(queueConfigId, username, now);
    }

    public LeadQueue save(LeadQueue row) {
        return leadQueueRepository.save(row);
    }

    /**
     * The caller's current non-expired claim in this queue (0 or 1 row per policy), lowest position first.
     */
    public Optional<LeadQueue> findMyActiveClaimInQueue(Long queueConfigId, String username, LocalDateTime now) {
        return leadQueueRepository
                .listActiveClaimsForUserInQueue(
                        queueConfigId, username, now, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    /**
     * Whether the user has a different n_lead_queue row (id != {@code excludeLeadQueueId})
     * in this queue with a non-expired claim.
     */
    public boolean hasOtherActiveClaimInQueueExcluding(
            Long queueConfigId, String username, LocalDateTime now, long excludeLeadQueueId) {
        return leadQueueRepository.countOtherActiveClaimsInQueueExcludingId(
                        queueConfigId, username, now, excludeLeadQueueId)
                > 0;
    }
}