package com.nivasafinance.features.bre.repository;

import com.nivasafinance.features.bre.entity.BREConfigs;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface BREConfigRepository extends JpaRepository<BREConfigs, Long> {
    Optional<BREConfigs> findByUname(String uname);
}
