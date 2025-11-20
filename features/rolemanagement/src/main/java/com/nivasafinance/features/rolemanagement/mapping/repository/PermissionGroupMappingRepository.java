package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionGroupMappingRepository extends JpaRepository<PermissionGroupMapping, Long> {
    List<PermissionGroupMapping> findByPermissionGroupIdIn(List<Long> groupIds);
}

