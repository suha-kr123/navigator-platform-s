package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.mapping.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {
    Optional<UserRoleMapping> findByUsername(String username);
}

