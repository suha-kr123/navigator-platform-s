package com.nivasafinance.features.staff.repository;

import com.nivasafinance.features.staff.entity.Staff;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface StaffRepository extends JpaRepository<Staff, Long> {

    @Query("SELECT s FROM Staff s WHERE s.officeKey = :officeKey AND s.isDeleted = false")
    List<Staff> findAllByOfficeKey(@Param("officeKey") String officeKey);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM n_staff s WHERE s.user_id = :userId AND s.office_key = :officeKey AND s.is_deleted = false)",
           nativeQuery = true)
    boolean existsByUserIdAndOfficeKey(@Param("userId") Long userId, @Param("officeKey") String officeKey);

    @Query("SELECT s FROM Staff s WHERE s.userId = :userId AND s.isDeleted = false")
    Optional<Staff> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Staff s WHERE s.identifier = :identifier AND s.isDeleted = false")
    Optional<Staff> findByIdentifier(@Param("identifier") UUID identifier);

    @Query("SELECT s FROM Staff s WHERE s.identifier = :identifier")
    Optional<Staff> findByIdentifierIncludingDeleted(@Param("identifier") UUID identifier);

    // Unfiltered — used by admin delete/undo-delete and creation uniqueness checks
    @Query("SELECT s FROM Staff s WHERE s.userId = :userId")
    Optional<Staff> findByUserIdIncludingDeleted(@Param("userId") Long userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM n_staff s WHERE s.user_id = :userId AND s.office_key = :officeKey)",
           nativeQuery = true)
    boolean existsByUserIdAndOfficeKeyIncludingDeleted(@Param("userId") Long userId, @Param("officeKey") String officeKey);

}