package com.nivasafinance.features.leadqueues.repository;

import com.nivasafinance.features.leadqueues.entity.LeadQueue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class LeadQueueRepositoryImpl implements LeadQueueRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LeadQueue> findPageByQueueId(Long queueConfigId, int limit, int offset) {
        return entityManager.createQuery(
                """
                SELECT lq FROM LeadQueue lq
                WHERE lq.queueConfigId = :qid AND lq.isActive = true
                ORDER BY lq.position ASC, lq.id ASC
                """, LeadQueue.class)
                .setParameter("qid", queueConfigId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<LeadQueue> findPageByQueueIdForUser(
            Long queueConfigId, String username, LocalDateTime now, int limit, int offset) {
        return entityManager
                .createQuery(
                        """
                SELECT lq FROM LeadQueue lq
                WHERE lq.queueConfigId = :qid
                  AND lq.isActive = true
                  AND (lq.currentlyClaimedBy IS NULL
                    OR lq.claimExpiryAt IS NULL
                    OR lq.claimExpiryAt <= :now
                    OR lq.currentlyClaimedBy = :username)
                ORDER BY lq.position ASC, lq.id ASC
                """, LeadQueue.class)
                .setParameter("qid", queueConfigId)
                .setParameter("now", now)
                .setParameter("username", username)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public Optional<LeadQueue> findFirstClaimableByQueueId(Long queueConfigId, String username, LocalDateTime now) {
        List<LeadQueue> list = entityManager.createQuery(
                """
                SELECT lq FROM LeadQueue lq
                WHERE lq.queueConfigId = :qid
                  AND lq.isActive = true
                  AND (lq.currentlyClaimedBy IS NULL
                    OR lq.claimExpiryAt IS NULL
                    OR lq.claimExpiryAt <= :now
                    OR lq.currentlyClaimedBy = :username)
                ORDER BY lq.position ASC, lq.id ASC
                """, LeadQueue.class)
                .setParameter("qid", queueConfigId)
                .setParameter("now", now)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public Optional<Long> findFirstClaimableIdForUpdateSkipLocked(
            Long queueConfigId, String username, LocalDateTime now) {
        @SuppressWarnings("unchecked")
        List<?> rows = entityManager.createNativeQuery(
                """
                SELECT lq.id
                FROM n_lead_queue lq
                WHERE lq.queue_config_id = :qid
                  AND lq.is_active = true
                  AND (lq.currently_claimed_by IS NULL
                    OR lq.claim_expiry_at IS NULL
                    OR lq.claim_expiry_at <= :now
                    OR lq.currently_claimed_by = :username)
                ORDER BY lq.position ASC, lq.id ASC
                FOR UPDATE OF lq SKIP LOCKED
                LIMIT 1
                """)
                .setParameter("qid", queueConfigId)
                .setParameter("now", now)
                .setParameter("username", username)
                .getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object o = rows.get(0);
        if (o instanceof Number n) {
            return Optional.of(n.longValue());
        }
        return Optional.of(Long.parseLong(o.toString()));
    }
}
