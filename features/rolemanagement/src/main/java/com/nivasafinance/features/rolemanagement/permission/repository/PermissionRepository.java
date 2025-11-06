package com.nivasafinance.features.rolemanagement.permission.repository;

import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    List<Permission> findByIdIn(List<UUID> ids);
}

