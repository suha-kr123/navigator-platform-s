package com.nivasafinance.features.staff.repository;

import com.nivasafinance.features.staff.entity.Staff;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findAllByOfficeKey(String officeKey);

    boolean existsByUserIdAndOfficeKey(Long userId, String officeKey);

    Optional<Staff> findByUserId(Long userId);
}