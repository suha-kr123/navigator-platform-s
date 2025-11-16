package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterCodeValueRepository extends JpaRepository<MasterCodeValue, Long> {
    Optional<MasterCodeValue> findByKey(String key);
    List<MasterCodeValue> findByCodeKey(String codeKey);
    Optional<MasterCodeValue> findByKeyAndCodeKey(String key, String codeKey);
    List<MasterCodeValue> findByCodeKeyAndIsActiveTrue(String codeKey);
}

