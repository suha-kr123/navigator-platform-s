package com.nivasafinance.features.bre.repository;

import com.nivasafinance.features.bre.entity.BRELogs;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface BRELogRepository extends JpaRepository<BRELogs, Long> {
}
