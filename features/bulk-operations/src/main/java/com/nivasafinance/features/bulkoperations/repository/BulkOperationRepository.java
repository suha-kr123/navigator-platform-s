package com.nivasafinance.features.bulkoperations.repository;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.enums.BulkOperationStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BulkOperationRepository extends JpaRepository<BulkOperation, Long> {
    
    Optional<BulkOperation> findByOperationIdentifier(UUID operationIdentifier);
    
    List<BulkOperation> findByCreatedByAndFileHashAndCreatedAtAfter(
        String createdBy,
        String fileHash,
        LocalDateTime createdAt
    );

    /**
     * Find operations by user, file hash, and time window, with status in the given list.
     * Used for duplicate check: only completed operations count as duplicates.
     */
    List<BulkOperation> findByCreatedByAndFileHashAndCreatedAtAfterAndStatusIn(
        String createdBy,
        String fileHash,
        LocalDateTime createdAt,
        List<BulkOperationStatus> statuses
    );
    
    List<BulkOperation> findByStatusAndCreatedAtBefore(
        BulkOperationStatus status,
        LocalDateTime createdAt
    );
    
    List<BulkOperation> findByStatusOrderByCreatedAtAsc(BulkOperationStatus status);
    
    @Query("SELECT COUNT(b) FROM BulkOperation b WHERE b.createdBy = :username AND b.status IN :statuses")
    long countByCreatedByAndStatusIn(@Param("username") String username, @Param("statuses") List<BulkOperationStatus> statuses);
    
    /**
     * Find operations that have timed out and are still in progress.
     */
    @Query("SELECT b FROM BulkOperation b WHERE b.status IN :statuses AND b.timeoutAt IS NOT NULL AND b.timeoutAt < :now")
    List<BulkOperation> findTimedOutOperations(@Param("statuses") List<BulkOperationStatus> statuses, @Param("now") LocalDateTime now);
    
    /**
     * Find operations by user with pagination support.
     */
    Page<BulkOperation> findByCreatedByOrderByCreatedAtDesc(String username, Pageable pageable);
    
    /**
     * Find operations that need file cleanup (completed and older than retention period).
     * Includes operations with either upload file or report file to clean.
     */
    @Query("SELECT b FROM BulkOperation b WHERE b.status IN :terminalStatuses AND b.createdAt < :cutoffDate " +
            "AND (b.fileStorageKey IS NOT NULL OR b.summaryStorageKey IS NOT NULL)")
    List<BulkOperation> findOperationsForFileCleanup(@Param("terminalStatuses") List<BulkOperationStatus> terminalStatuses, @Param("cutoffDate") LocalDateTime cutoffDate);
}
