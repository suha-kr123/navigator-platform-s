package com.nivasafinance.features.consent.repository;

import com.nivasafinance.features.consent.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {

    Optional<Consent> findByIdentifier(UUID identifier);
}
