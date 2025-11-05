package com.nivasafinance.features.stage.repository;

import com.nivasafinance.features.stage.entity.StageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StageConfigRepository extends JpaRepository<StageConfig, Long> {
    
    Optional<StageConfig> findByKeyAndIsActive(String key, Boolean isActive);
}

