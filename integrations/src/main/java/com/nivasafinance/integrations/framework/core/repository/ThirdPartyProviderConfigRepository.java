package com.nivasafinance.integrations.framework.core.repository;

import com.nivasafinance.integrations.framework.core.entity.ThirdPartyProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThirdPartyProviderConfigRepository extends JpaRepository<ThirdPartyProviderConfig, UUID> {
}

