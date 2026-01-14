package com.nivasafinance.features.usermanagement.repository;

import com.nivasafinance.features.usermanagement.entity.User;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query(value = "SELECT u.* FROM n_user u " +
           "JOIN n_person p ON p.id = u.person_id " +
           "WHERE EXISTS (" +
           "  SELECT 1 FROM jsonb_array_elements(COALESCE(p.mobile_numbers, '[]'::jsonb)) AS m " +
           "  WHERE RIGHT(REGEXP_REPLACE(m->>'number', '[^0-9]', '', 'g'), 10) = " +
           "        RIGHT(REGEXP_REPLACE(:phoneNumber, '[^0-9]', '', 'g'), 10) " +
           "  AND (m->>'isPrimary')::boolean = true" +
           ")", nativeQuery = true)
    List<User> findByPersonPhoneNumber(@Param("phoneNumber") String phoneNumber);
}

