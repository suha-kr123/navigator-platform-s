package com.nivasafinance.common.repository;

import com.nivasafinance.common.audit.ApiAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAuditLogRepository extends JpaRepository<ApiAuditLog, Long> {
}

