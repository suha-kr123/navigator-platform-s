package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLog;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface CallLogRepository extends JpaRepository<CallLog, Long> {
    Optional<CallLog> findByIdentifier(UUID identifier);
    Optional<CallLog> findByProviderId(String providerId);
}

