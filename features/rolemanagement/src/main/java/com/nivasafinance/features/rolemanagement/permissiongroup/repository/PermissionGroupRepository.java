package com.nivasafinance.features.rolemanagement.permissiongroup.repository;

import com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {
    Page<PermissionGroup> findAll(Pageable pageable);
    Page<PermissionGroup> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
