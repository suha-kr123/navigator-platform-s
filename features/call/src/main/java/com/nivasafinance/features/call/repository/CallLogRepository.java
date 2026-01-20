package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLog;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface CallLogRepository extends JpaRepository<CallLog, Long> {
    Optional<CallLog> findByIdentifier(UUID identifier);
    Optional<CallLog> findByProviderId(String providerId);
    
    @org.springframework.data.jpa.repository.Query("SELECT cl FROM CallLog cl WHERE cl.fromNumber = :phoneNumber OR cl.toNumber = :phoneNumber ORDER BY cl.createdAt DESC")
    List<CallLog> findByPhoneNumber(@org.springframework.data.repository.query.Param("phoneNumber") String phoneNumber);
}

