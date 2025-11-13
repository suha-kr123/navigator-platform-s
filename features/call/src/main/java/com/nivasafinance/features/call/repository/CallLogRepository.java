package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLog;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface CallLogRepository extends JpaRepository<CallLog, Long> {
}

