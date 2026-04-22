package com.nivasafinance.features.leadqueues.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.leadqueues.dto.LeadQueueResponse;
import com.nivasafinance.features.leadqueues.dto.QueueConfigResponse;
import com.nivasafinance.features.leadqueues.dto.QueueWorkbenchResponse;
import com.nivasafinance.features.leadqueues.entity.LeadQueue;
import com.nivasafinance.features.leadqueues.entity.QueueConfig;
import com.nivasafinance.features.leadqueues.exception.LeadQueueExceptionFactory;
import com.nivasafinance.features.leadqueues.repository.LeadQueueRepositoryWrapper;
import com.nivasafinance.features.leadqueues.repository.QueueConfigRepositoryWrapper;
import com.nivasafinance.features.leadqueues.service.QueueReorderService;
import com.nivasafinance.features.leadqueues.service.QueueService;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    /** How many listable rows to scan when picking “next” after the active claim. */
    private static final int WORKBENCH_LIST_SCAN = 200;

    private final LeadQueueRepositoryWrapper leadQueueRepositoryWrapper;
    private final QueueConfigRepositoryWrapper queueConfigRepositoryWrapper;
    private final QueueReorderService queueReorderService;
    private final LeadReadService leadReadService;
    private final UserReadService userReadService;
    private final MessageSource messageSource;
    private final LeadRepositoryWrapper leadRepositoryWrapper;

    @Override
    public List<QueueConfigResponse> fetchQueueForAgent(String username) {
        Optional<User> user = userReadService.findUserByUsername(username);
        if (user.isEmpty()) {
            return List.of();
        }
        return queueConfigRepositoryWrapper.findAllActiveByUserId(user.get().getId())
                .stream()
                .map(c -> QueueConfigResponse.builder()
                        .queueConfigName(c.getQueueName())
                        .description(c.getDescription())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public PaginatedResponse<LeadQueueResponse> getLeadsForQueue(String queueConfigName, PaginationRequest paginationRequest) {
        Long userId = requireUserId();
        QueueConfig config = queueConfigRepositoryWrapper.findByQueueName(queueConfigName);
        assertUserHasQueueAccess(config, userId);

        try {
            // Refresh positions if past reorder window: last_reorder_time + reorder_time (seconds)
            queueReorderService.reorderQueue(queueConfigName);
        } catch (Exception e) {
            log.warn("Queue reorder on list failed for {} — returning last known order", queueConfigName, e);
        }

        int offset = Math.max(0, paginationRequest.getOffset());
        int limit = Math.max(1, paginationRequest.getLimit());
        String username =
                userReadService.findUserByUsername(UserContext.getUsername()).map(User::getUsername).orElse("");
        LocalDateTime now = LocalDateTime.now();
        // Hide leads that are held by *another* user (active claim) — same predicate as "claimable" list
        long total = leadQueueRepositoryWrapper.countListableByQueueIdForUser(config.getId(), username, now);
        if (total == 0) {
            return new PaginatedResponse<>(
                    List.of(),
                    buildPaginationInfo(paginationRequest, 0L));
        }

        List<LeadQueue> page =
                leadQueueRepositoryWrapper.findPageByQueueIdForUser(config.getId(), username, now, limit, offset);
        String name = config.getQueueName();
        List<LeadQueueResponse> content = new ArrayList<>(page.size());
        for (LeadQueue row : page) {
            content.add(toResponse(row, name, loadLeadResponse(row.getLeadId())));
        }
        return new PaginatedResponse<>(content, buildPaginationInfo(paginationRequest, total));
    }

    @Override
    @Transactional
    public QueueWorkbenchResponse getQueueWorkbench(String queueConfigName, String username) {
        Long userId = requireUserIdOrUsername(username);
        QueueConfig config = queueConfigRepositoryWrapper.findByQueueName(queueConfigName);
        assertUserHasQueueAccess(config, userId);
        try {
            queueReorderService.reorderQueue(queueConfigName);
        } catch (Exception e) {
            log.warn("Queue reorder on workbench failed for {} — continuing with last order", queueConfigName, e);
        }
        LocalDateTime now = LocalDateTime.now();
        String queueName = config.getQueueName();
        Optional<LeadQueue> activeLq =
                leadQueueRepositoryWrapper.findMyActiveClaimInQueue(config.getId(), username, now);
        List<LeadQueue> scan = leadQueueRepositoryWrapper.findPageByQueueIdForUser(
                config.getId(), username, now, WORKBENCH_LIST_SCAN, 0);
        Long activeRowId = activeLq.map(LeadQueue::getId).orElse(null);
        Optional<LeadQueue> nextLq = scan.stream()
                .filter(lq -> activeRowId == null || !Objects.equals(lq.getId(), activeRowId))
                .findFirst();
        return QueueWorkbenchResponse.builder()
                .currentClaim(activeLq
                        .map(lq -> toResponse(lq, queueName, loadLeadResponse(lq.getLeadId())))
                        .orElse(null))
                .nextAvailable(
                        nextLq.map(lq -> toResponse(lq, queueName, loadLeadResponse(lq.getLeadId()))).orElse(null))
                .build();
    }

    @Override
    @Transactional
    public LeadQueueResponse claimLead(UUID leadIdentifier, String queueConfigName, String username) {
        Long userId = requireUserIdOrUsername(username);
        QueueConfig config = queueConfigRepositoryWrapper.findByQueueName(queueConfigName);
        assertUserHasQueueAccess(config, userId);
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        LeadQueue lq = leadQueueRepositoryWrapper
                .findByQueueConfigIdAndLeadIdAndIsActiveTrue(config.getId(), lead.getId())
                .orElseThrow(() -> LeadQueueExceptionFactory.leadNotInQueue(queueConfigName, messageSource));

        LocalDateTime now = LocalDateTime.now();
        // Avoid locking this row if we are going to work another lead; reduces contention.
        if (isActivelyLockedByAnotherUser(lq, now, username)) {
            return claimFirstAvailableInQueue(config, username, now);
        }

        // Serialize competing claims for this queue entry; recheck after lock in case of a race.
        LeadQueue locked = leadQueueRepositoryWrapper
                .findByIdAndIsActiveForUpdate(lq.getId())
                .orElseThrow(() -> LeadQueueExceptionFactory.leadNotInQueue(queueConfigName, messageSource));
        if (isActivelyLockedByAnotherUser(locked, now, username)) {
            return claimFirstAvailableInQueue(config, username, now);
        }

        if (isActivelyLockedByThisUser(locked, now, username)) {
            extendClaim(locked, config);
            return toResponse(locked, config.getQueueName(), loadLeadResponse(locked.getLeadId()));
        }

        applyNewClaim(locked, config, username, now);
        return toResponse(locked, config.getQueueName(), loadLeadResponse(locked.getLeadId()));
    }

    @Override
    @Transactional
    public void releaseLead(UUID leadIdentifier, String queueConfigName, String username) {
        Long userId = requireUserIdOrUsername(username);
        QueueConfig config = queueConfigRepositoryWrapper.findByQueueName(queueConfigName);
        assertUserHasQueueAccess(config, userId);
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        LeadQueue lq = leadQueueRepositoryWrapper
                .findByQueueConfigIdAndLeadIdAndIsActiveTrue(config.getId(), lead.getId())
                .orElseThrow(() -> LeadQueueExceptionFactory.leadNotInQueue(queueConfigName, messageSource));

        if (lq.getCurrentlyClaimedBy() == null) {
            throw LeadQueueExceptionFactory.notClaimed(messageSource);
        }
        if (!username.equals(lq.getCurrentlyClaimedBy())) {
            throw LeadQueueExceptionFactory.releaseNotHolder(messageSource);
        }

        completeLastHistoryEvent(lq, username);
        lq.setCurrentlyClaimedBy(null);
        lq.setClaimExpiryAt(null);
        leadQueueRepositoryWrapper.save(lq);
    }

    @Override
    @Transactional
    public void heartbeat(UUID leadIdentifier, String queueConfigName, String username) {
        Long userId = requireUserIdOrUsername(username);
        QueueConfig config = queueConfigRepositoryWrapper.findByQueueName(queueConfigName);
        assertUserHasQueueAccess(config, userId);
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        LeadQueue lq = leadQueueRepositoryWrapper
                .findByQueueConfigIdAndLeadIdAndIsActiveTrue(config.getId(), lead.getId())
                .orElseThrow(() -> LeadQueueExceptionFactory.leadNotInQueue(queueConfigName, messageSource));

        if (lq.getCurrentlyClaimedBy() == null) {
            throw LeadQueueExceptionFactory.heartbeatNotClaimed(messageSource);
        }
        if (!username.equals(lq.getCurrentlyClaimedBy())) {
            throw LeadQueueExceptionFactory.heartbeatNotHolder(messageSource);
        }
        extendClaim(lq, config);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadQueueResponse getLeadQueueResponse(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        List<LeadQueue> rows = leadQueueRepositoryWrapper.findByLeadIdAndIsActiveTrueOrderByPosition(lead.getId());
        if (rows.isEmpty()) {
            throw LeadQueueExceptionFactory.noQueueEntryForLead(messageSource);
        }
        LeadQueue row = rows.get(0);
        String queueName = queueConfigRepositoryWrapper.getQueueNameByIdOrEmpty(row.getQueueConfigId());
        return toResponse(row, queueName, leadReadService.getLeadByIdentifier(leadIdentifier));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeadQueueResponse> getMyActiveClaim(String queueConfigName, String username) {
        Long userId = requireUserIdOrUsername(username);
        QueueConfig config = queueConfigRepositoryWrapper.findByQueueName(queueConfigName);
        assertUserHasQueueAccess(config, userId);
        LocalDateTime now = LocalDateTime.now();
        return leadQueueRepositoryWrapper
                .findMyActiveClaimInQueue(config.getId(), username, now)
                .map(lq -> toResponse(lq, config.getQueueName(), loadLeadResponse(lq.getLeadId())));
    }

    private LeadResponse loadLeadResponse(Long leadId) {
        Lead lead = leadRepositoryWrapper.findByIdWithException(leadId);
        return leadReadService.getLeadByIdentifier(lead.getLeadIdentifier());
    }

    private static boolean isActivelyLockedByThisUser(LeadQueue lq, LocalDateTime now, String username) {
        if (lq.getCurrentlyClaimedBy() == null || lq.getClaimExpiryAt() == null) {
            return false;
        }
        if (!lq.getClaimExpiryAt().isAfter(now)) {
            return false;
        }
        return username.equals(lq.getCurrentlyClaimedBy());
    }

    private static boolean isActivelyLockedByAnotherUser(LeadQueue lq, LocalDateTime now, String username) {
        if (lq.getCurrentlyClaimedBy() == null || lq.getClaimExpiryAt() == null) {
            return false;
        }
        if (!lq.getClaimExpiryAt().isAfter(now)) {
            return false;
        }
        return !username.equals(lq.getCurrentlyClaimedBy());
    }

    private LeadQueueResponse claimFirstAvailableInQueue(QueueConfig config, String username, LocalDateTime now) {
        String queueName = config.getQueueName();
        Long firstId = leadQueueRepositoryWrapper
                .findFirstClaimableIdForUpdateSkipLocked(config.getId(), username, now)
                .orElseThrow(() -> LeadQueueExceptionFactory.noClaimableWork(queueName, messageSource));

        LeadQueue target = leadQueueRepositoryWrapper
                .findByIdAndIsActiveForUpdate(firstId)
                .orElseThrow(() -> LeadQueueExceptionFactory.noClaimableWork(queueName, messageSource));
        if (isActivelyLockedByThisUser(target, now, username)) {
            extendClaim(target, config);
        } else {
            applyNewClaim(target, config, username, now);
        }
        return toResponse(target, queueName, loadLeadResponse(target.getLeadId()));
    }

    private void applyNewClaim(LeadQueue lq, QueueConfig config, String username, LocalDateTime now) {
        if (leadQueueRepositoryWrapper.hasOtherActiveClaimInQueueExcluding(
                config.getId(), username, now, lq.getId())) {
            throw LeadQueueExceptionFactory.mustReleaseActiveClaimFirst(config.getQueueName(), messageSource);
        }
        appendNewClaimEvent(lq, username);
        lq.setCurrentlyClaimedBy(username);
        lq.setClaimExpiryAt(now.plusMinutes(safeLockMinutes(config)));
        leadQueueRepositoryWrapper.save(lq);
    }

    private Long requireUserId() {
        return userReadService.findUserByUsername(UserContext.getUsername())
                .map(User::getId)
                .orElse(null);
    }

    private Long requireUserIdOrUsername(String username) {
        return userReadService.findUserByUsername(username)
                .map(User::getId)
                .orElse(null);
    }

    private void assertUserHasQueueAccess(QueueConfig config, Long userId) {
        if (userId == null) {
            throw LeadQueueExceptionFactory.queueAccessDenied(messageSource);
        }
        if (config.getUserIds() == null || !config.getUserIds().contains(userId)) {
            throw LeadQueueExceptionFactory.queueAccessDenied(messageSource);
        }
    }

    private void extendClaim(LeadQueue lq, QueueConfig config) {
        LocalDateTime now = LocalDateTime.now();
        lq.setClaimExpiryAt(now.plusMinutes(safeLockMinutes(config)));
        leadQueueRepositoryWrapper.save(lq);
    }

    private static int safeLockMinutes(QueueConfig config) {
        Integer m = config.getLockDuration();
        if (m == null || m <= 0) {
            return 10;
        }
        return m;
    }

    private void appendNewClaimEvent(LeadQueue lq, String username) {
        List<LeadQueue.ClaimHistory> list = lq.getClaimedHistory() != null
                ? new ArrayList<>(lq.getClaimedHistory())
                : new ArrayList<>();
        list.add(new LeadQueue.ClaimHistory(LocalDateTime.now(), username, null, null));
        lq.setClaimedHistory(list);
    }

    private void completeLastHistoryEvent(LeadQueue lq, String username) {
        if (lq.getClaimedHistory() == null || lq.getClaimedHistory().isEmpty()) {
            return;
        }
        int last = lq.getClaimedHistory().size() - 1;
        LeadQueue.ClaimHistory h = lq.getClaimedHistory().get(last);
        h.setUnclaimedAt(LocalDateTime.now());
        h.setUnclaimedBy(username);
    }

    private LeadQueueResponse toResponse(LeadQueue row, String queueConfigName, LeadResponse lead) {
        return LeadQueueResponse.builder()
                .queueConfigName(queueConfigName)
                .leadIdentifier(lead.getLeadIdentifier())
                .lead(lead)
                .calculatedAt(row.getCalculatedAt())
                .position(row.getPosition())
                .currentlyClaimedBy(row.getCurrentlyClaimedBy())
                .claimExpiryAt(row.getClaimExpiryAt())
                .claimedHistory(mapHistory(row.getClaimedHistory()))
                .build();
    }

    private List<LeadQueueResponse.ClaimHistory> mapHistory(List<LeadQueue.ClaimHistory> in) {
        if (in == null) {
            return null;
        }
        return in.stream()
                .map(h -> LeadQueueResponse.ClaimHistory.builder()
                        .claimedAt(h.getClaimedAt())
                        .claimedBy(h.getClaimedBy())
                        .unclaimedBy(h.getUnclaimedBy())
                        .unclaimedAt(h.getUnclaimedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private static PaginationInfo buildPaginationInfo(PaginationRequest paginationRequest, long total) {
        int limit = Math.max(1, paginationRequest.getLimit());
        int offset = Math.max(0, paginationRequest.getOffset());
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) total / limit);
        int currentPage = limit == 0 ? 0 : offset / limit;
        boolean hasNext = offset + limit < total;
        boolean hasPrevious = offset > 0;
        return new PaginationInfo(
                offset,
                limit,
                total,
                totalPages,
                currentPage,
                hasNext,
                hasPrevious
        );
    }
}
