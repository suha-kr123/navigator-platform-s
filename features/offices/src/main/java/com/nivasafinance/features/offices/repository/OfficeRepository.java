package com.nivasafinance.features.offices.repository;

import com.nivasafinance.features.offices.entity.Office;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface OfficeRepository extends JpaRepository<Office, Long> {

    Optional<Office> findByKey(String key);

    @Query("SELECT o FROM Office o WHERE o.parentId IS NULL ORDER BY o.code DESC")
    List<Office> findByParentIdIsNullOrderByCodeDesc();

    @Query("SELECT o FROM Office o WHERE o.parentId = :parentId ORDER BY o.code DESC")
    List<Office> findByParentIdOrderByCodeDesc(@Param("parentId") Long parentId);
}

