package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findAllByIsActiveTrue();
    List<District> findByStateIdAndIsActiveTrue(Long stateId);
    java.util.Optional<District> findByCodeAndStateIdAndIsActiveTrue(String code, Long stateId);

    @Query("SELECT DISTINCT d FROM District d WHERE d.stateId = :stateId AND d.isActive = true "
            + "AND EXISTS (SELECT 1 FROM com.nivasafinance.features.master.pincode.entity.Pincode p "
            + "WHERE p.districtId = d.id AND p.isServicable = true)")
    List<District> findServiceableDistrictsByStateId(@Param("stateId") Long stateId);
}

