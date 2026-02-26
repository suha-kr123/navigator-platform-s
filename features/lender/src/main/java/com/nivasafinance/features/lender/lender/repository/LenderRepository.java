package com.nivasafinance.features.lender.lender.repository;

import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LenderRepository extends JpaRepository<Lender, UUID> {
    Optional<Lender> findByKey(String key);
    Optional<Lender> findByName(String name);
    List<Lender> findByStatus(LenderStatus status);
    Page<Lender> findByStatus(LenderStatus status, Pageable pageable);
}

