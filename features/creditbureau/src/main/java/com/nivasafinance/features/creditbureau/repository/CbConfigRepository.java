package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CbConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CbConfigRepository extends JpaRepository<CbConfig, Long> {

    Optional<CbConfig> findByConfigKey(String configKey);
}
