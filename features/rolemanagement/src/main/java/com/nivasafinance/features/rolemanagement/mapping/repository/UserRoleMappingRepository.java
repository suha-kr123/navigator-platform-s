package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.mapping.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {

    List<UserRoleMapping> findByUsername(String username);
    Optional<UserRoleMapping> findByUsernameAndIsPrimary(String username, Boolean isPrimary);
    List<UserRoleMapping> findByRoleIn(List<String> roles);
    Optional<UserRoleMapping> findByUsernameAndRole(String username, String role);
    boolean existsByUsernameAndRole(String username, String role);
    void deleteByUsernameAndRole(String username, String role);
}
