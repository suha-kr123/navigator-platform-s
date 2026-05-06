package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.Taluka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TalukaRepository extends JpaRepository<Taluka, Long> {
    List<Taluka> findAllByIsActiveTrue();
    List<Taluka> findByDistrictIdAndIsActiveTrue(Long districtId);
    java.util.Optional<Taluka> findByCodeAndDistrictIdAndIsActiveTrue(String code, Long districtId);

    @Query("SELECT DISTINCT t FROM Taluka t WHERE t.districtId = :districtId AND t.isActive = true "
            + "AND EXISTS (SELECT 1 FROM com.nivasafinance.features.master.pincode.entity.Pincode p "
            + "WHERE p.talukaId = t.id AND p.isServicable = true)")
    List<Taluka> findServiceableTalukasByDistrictId(@Param("districtId") Long districtId);

    @Query("SELECT t.isServiceable FROM Taluka t JOIN t.district d "
            + "WHERE d.code = :districtCode AND t.code = :talukaCode "
            + "AND d.isActive = true AND t.isActive = true")
    Optional<Boolean> findServiceabilityByDistrictCodeAndTalukaCode(
            @Param("districtCode") String districtCode,
            @Param("talukaCode") String talukaCode);
}

