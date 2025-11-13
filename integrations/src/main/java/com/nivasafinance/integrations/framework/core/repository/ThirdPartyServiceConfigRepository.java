package com.nivasafinance.integrations.framework.core.repository;

import com.nivasafinance.integrations.framework.core.entity.ThirdPartyServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThirdPartyServiceConfigRepository extends JpaRepository<ThirdPartyServiceConfig, Long> {
    Optional<ThirdPartyServiceConfig> findByServiceAndIsActiveTrue(String serviceName);
}

