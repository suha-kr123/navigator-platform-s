package com.nivasafinance.features.advisoractivity.repository;

import com.nivasafinance.features.advisoractivity.entity.AdvisorActivity;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface AdvisorActivityRepository extends JpaRepository<AdvisorActivity, Long> {
}

