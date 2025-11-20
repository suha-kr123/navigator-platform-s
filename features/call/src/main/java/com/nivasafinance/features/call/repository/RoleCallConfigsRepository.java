package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.RoleCallConfigs;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface RoleCallConfigsRepository extends JpaRepository<RoleCallConfigs, Long> {

    Optional<RoleCallConfigs> findByRole(String role);
}

