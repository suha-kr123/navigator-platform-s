package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterCodeRepository extends JpaRepository<MasterCode, UUID> {
    Optional<MasterCode> findByKey(String key);
    List<MasterCode> findByParentId(UUID parentId);
}

