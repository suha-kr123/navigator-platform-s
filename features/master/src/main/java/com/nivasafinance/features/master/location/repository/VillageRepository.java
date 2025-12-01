package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, Long> {
    List<Village> findAllByIsActiveTrue();
    List<Village> findByTalukaIdAndIsActiveTrue(Long talukaId);
    java.util.Optional<Village> findByCodeAndTalukaIdAndIsActiveTrue(String code, Long talukaId);
}

