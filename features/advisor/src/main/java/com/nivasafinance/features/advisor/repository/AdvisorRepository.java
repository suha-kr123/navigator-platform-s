package com.nivasafinance.features.advisor.repository;

import com.nivasafinance.features.advisor.entity.Advisor;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface AdvisorRepository extends JpaRepository<Advisor, UUID> {
    @Query("SELECT a FROM Advisor a WHERE a.identifier = :identifier AND a.isDeleted = false")
    Optional<Advisor> findByIdentifier(@Param("identifier") UUID identifier);

    @Query("SELECT a FROM Advisor a WHERE a.username = :username AND a.isDeleted = false")
    Optional<Advisor> findByUsername(@Param("username") String username);

    // Unfiltered — used by admin delete/undo-delete and creation uniqueness checks
    @Query("SELECT a FROM Advisor a WHERE a.identifier = :identifier")
    Optional<Advisor> findByIdentifierIncludingDeleted(@Param("identifier") UUID identifier);

    @Query("SELECT a FROM Advisor a WHERE a.username = :username")
    Optional<Advisor> findByUsernameIncludingDeleted(@Param("username") String username);
}

