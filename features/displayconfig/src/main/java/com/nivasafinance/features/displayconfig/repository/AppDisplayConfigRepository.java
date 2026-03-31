package com.nivasafinance.features.displayconfig.repository;

import com.nivasafinance.features.displayconfig.entity.AppDisplayConfig;
import com.nivasafinance.features.displayconfig.enums.AppType;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface AppDisplayConfigRepository extends JpaRepository<AppDisplayConfig, Long> {

    Optional<AppDisplayConfig> findByAppTypeAndIsActiveTrue(AppType appType);
}
