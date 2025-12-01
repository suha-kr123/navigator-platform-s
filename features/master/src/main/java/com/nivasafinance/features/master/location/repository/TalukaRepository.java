package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.Taluka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalukaRepository extends JpaRepository<Taluka, Long> {
    List<Taluka> findAllByIsActiveTrue();
    List<Taluka> findByDistrictIdAndIsActiveTrue(Long districtId);
}

