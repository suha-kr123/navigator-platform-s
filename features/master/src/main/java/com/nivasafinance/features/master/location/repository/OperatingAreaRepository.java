package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.OperatingArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatingAreaRepository extends JpaRepository<OperatingArea, Long> {
    List<OperatingArea> findByRegionIdAndIsActiveTrue(Long regionId);
    Optional<OperatingArea> findByCodeAndRegionIdAndIsActiveTrue(String code, Long regionId);
}
