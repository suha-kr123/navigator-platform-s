package com.nivasafinance.features.leadqueues.repository;

import com.nivasafinance.features.leadqueues.entity.LeadQueue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * {@link org.springframework.data.jpa.repository.JpaRepository} add-on for
 * paged/claimable queries (JPQL, no {@code SELECT *}).
 */
public interface LeadQueueRepositoryCustom {

    List<LeadQueue> findPageByQueueId(Long queueConfigId, int limit, int offset);

    /**
     * Same list semantics as {@link #countListableByQueueIdForUser} on
     * {@link com.nivasafinance.features.leadqueues.repository.LeadQueueRepository}.
     */
    List<LeadQueue> findPageByQueueIdForUser(
            Long queueConfigId, String username, LocalDateTime now, int limit, int offset);

    Optional<LeadQueue> findFirstClaimableByQueueId(Long queueConfigId, String username, LocalDateTime now);

    /**
     * Picks the first lead-queue row the caller may work (in position order) and takes a row lock
     * ({@code FOR UPDATE SKIP LOCKED}) so concurrent agents receive different rows instead of
     * overwriting the same one.
     */
    Optional<Long> findFirstClaimableIdForUpdateSkipLocked(Long queueConfigId, String username, LocalDateTime now);
}
