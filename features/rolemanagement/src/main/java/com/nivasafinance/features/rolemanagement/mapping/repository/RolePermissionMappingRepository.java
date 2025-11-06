package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolePermissionMappingRepository extends JpaRepository<RolePermissionMapping, UUID> {
    List<RolePermissionMapping> findByRoleIdIn(List<UUID> roleIds);
}

