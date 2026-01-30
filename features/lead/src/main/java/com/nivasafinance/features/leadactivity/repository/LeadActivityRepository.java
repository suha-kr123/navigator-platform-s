package com.nivasafinance.features.leadactivity.repository;

import com.nivasafinance.features.leadactivity.entity.LeadActivity;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface LeadActivityRepository extends JpaRepository<LeadActivity, Long> {
}


