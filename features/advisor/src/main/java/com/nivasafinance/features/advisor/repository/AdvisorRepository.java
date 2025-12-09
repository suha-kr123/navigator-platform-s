package com.nivasafinance.features.advisor.repository;

import com.nivasafinance.features.advisor.entity.Advisor;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface AdvisorRepository extends JpaRepository<Advisor, UUID> {
    Optional<Advisor> findByIdentifier(UUID identifier);
    Optional<Advisor> findByPersonId(Long personId);
}

