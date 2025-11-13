package com.nivasafinance.integrations.framework.core.repository;

import com.nivasafinance.integrations.framework.core.entity.ThirdPartyResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThirdPartyResponseLogRepository extends JpaRepository<ThirdPartyResponseLog, Long> {
}

