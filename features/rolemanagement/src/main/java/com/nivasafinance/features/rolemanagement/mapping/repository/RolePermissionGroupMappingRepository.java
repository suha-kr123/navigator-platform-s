package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionGroupMappingRepository extends JpaRepository<RolePermissionGroupMapping, UUID> {
    List<RolePermissionGroupMapping> findByRoleIdIn(List<UUID> roleIds);
}

