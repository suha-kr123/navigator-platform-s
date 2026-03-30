package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallProvider;
import org.javers.spring.annotation.JaversSpringDataAuditable;
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
@JaversSpringDataAuditable
public interface CallLogRepository extends JpaRepository<CallLog, Long> {
    Optional<CallLog> findByIdentifier(UUID identifier);
    Optional<CallLog> findByProviderId(String providerId);
    
    @org.springframework.data.jpa.repository.Query("SELECT cl FROM CallLog cl WHERE cl.fromNumber = :phoneNumber OR cl.toNumber = :phoneNumber ORDER BY cl.createdAt DESC")
    List<CallLog> findByPhoneNumber(@org.springframework.data.repository.query.Param("phoneNumber") String phoneNumber);

    @Query("SELECT cl FROM CallLog cl WHERE cl.provider = :provider AND cl.createdAt >= :start AND cl.createdAt < :end ORDER BY cl.id ASC")
    Page<CallLog> findByProviderAndCreatedAtRange(
            @Param("provider") CallProvider provider,
            @Param("start") LocalDateTime startInclusive,
            @Param("end") LocalDateTime endExclusive,
            Pageable pageable);

    @Query("SELECT cl FROM CallLog cl WHERE cl.provider = :provider ORDER BY cl.id ASC")
    Page<CallLog> findByProviderOrderByIdAsc(
            @Param("provider") CallProvider provider,
            Pageable pageable);
}

