package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findAllByIsActiveTrue();
    List<District> findByStateIdAndIsActiveTrue(Long stateId);
    java.util.Optional<District> findByCodeAndStateIdAndIsActiveTrue(String code, Long stateId);
}

