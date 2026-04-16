package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    List<Region> findByStateIdAndIsActiveTrue(Long stateId);
    Optional<Region> findByCodeAndStateIdAndIsActiveTrue(String code, Long stateId);
}
