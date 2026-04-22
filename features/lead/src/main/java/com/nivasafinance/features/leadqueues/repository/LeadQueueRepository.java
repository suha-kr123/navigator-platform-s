package com.nivasafinance.features.leadqueues.repository;

import com.nivasafinance.features.leadqueues.entity.LeadQueue;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeadQueueRepository extends JpaRepository<LeadQueue, Long>, LeadQueueRepositoryCustom {

    long countByQueueConfigIdAndIsActiveTrue(Long queueConfigId);

    /**
     * Rows the viewer should see: unclaimed, claim expired, or currently claimed by this user.
     * Hides leads that are held by <em>another</em> user with a non-expired claim.
     */
    @Query(
            "select count(lq) from LeadQueue lq"
                    + " where lq.queueConfigId = :qid and lq.isActive = true"
                    + " and (lq.currentlyClaimedBy is null or lq.claimExpiryAt is null or lq.claimExpiryAt <= :now"
                    + " or lq.currentlyClaimedBy = :username)")
    long countListableByQueueIdForUser(
            @Param("qid") Long queueConfigId, @Param("username") String username, @Param("now") LocalDateTime now);

    Optional<LeadQueue> findByQueueConfigIdAndLeadIdAndIsActiveTrue(Long queueConfigId, Long leadId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lq from LeadQueue lq where lq.id = :id and lq.isActive = true")
    Optional<LeadQueue> findByIdAndIsActiveForUpdate(@Param("id") Long id);

    List<LeadQueue> findByLeadIdAndIsActiveTrueOrderByPositionAscIdAsc(Long leadId);

    /**
     * At most one row is expected in normal operation; ordered for deterministic selection.
     */
    @Query("select lq from LeadQueue lq"
            + " where lq.queueConfigId = :qid and lq.isActive = true"
            + " and lq.currentlyClaimedBy = :username and lq.claimExpiryAt is not null and lq.claimExpiryAt > :now"
            + " order by lq.position asc, lq.id asc")
    List<LeadQueue> listActiveClaimsForUserInQueue(
            @Param("qid") Long queueConfigId, @Param("username") String username, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("select count(lq) from LeadQueue lq"
            + " where lq.queueConfigId = :qid and lq.isActive = true"
            + " and lq.currentlyClaimedBy = :username and lq.claimExpiryAt is not null and lq.claimExpiryAt > :now"
            + " and lq.id <> :excludeLeadQueueId")
    long countOtherActiveClaimsInQueueExcludingId(
            @Param("qid") Long queueConfigId,
            @Param("username") String username,
            @Param("now") LocalDateTime now,
            @Param("excludeLeadQueueId") Long excludeLeadQueueId);
}
