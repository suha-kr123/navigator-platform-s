package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.OperatingArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatingAreaRepository extends JpaRepository<OperatingArea, Long> {
    List<OperatingArea> findByRegionIdAndIsActiveTrue(Long regionId);
    Optional<OperatingArea> findByCodeAndRegionIdAndIsActiveTrue(String code, Long regionId);

    @Query("SELECT COUNT(oa) > 0 FROM OperatingArea oa WHERE oa.code = :operatingAreaCode AND oa.isActive = true "
            + "AND EXISTS (SELECT 1 FROM com.nivasafinance.features.master.pincode.entity.Pincode p "
            + "WHERE p.operatingAreaId = oa.id AND p.isServicable = true)")
    boolean existsServiceableOperatingAreaByCode(@Param("operatingAreaCode") String operatingAreaCode);
}
