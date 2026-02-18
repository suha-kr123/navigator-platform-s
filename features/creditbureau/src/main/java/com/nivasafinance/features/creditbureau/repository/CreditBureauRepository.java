package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditBureauRepository extends JpaRepository<CreditBureauEnquiry, Long> {
    Optional<CreditBureauEnquiry> findByIdentifier(UUID identifier);
}

