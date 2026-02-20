package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterCodeValueRepository extends JpaRepository<MasterCodeValue, Long> {
    Optional<MasterCodeValue> findByKey(String key);
    List<MasterCodeValue> findByCodeKey(String codeKey);
    Page<MasterCodeValue> findByCodeKey(String codeKey, Pageable pageable);
    Optional<MasterCodeValue> findByKeyAndCodeKey(String key, String codeKey);
    List<MasterCodeValue> findByCodeKeyAndIsActiveTrue(String codeKey);
    Page<MasterCodeValue> findByCodeKeyAndIsActiveTrue(String codeKey, Pageable pageable);
}

