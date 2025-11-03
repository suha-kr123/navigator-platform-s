package com.nivasafinance.features.offices.repository;

import com.nivasafinance.features.offices.entity.Office;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface OfficeRepository extends JpaRepository<Office, UUID> {

    @Query("SELECT o FROM Office o WHERE o.parentId IS NULL ORDER BY o.code DESC")
    List<Office> findByParentIdIsNullOrderByCodeDesc();

    @Query("SELECT o FROM Office o WHERE o.parentId = :parentId ORDER BY o.code DESC")
    List<Office> findByParentIdOrderByCodeDesc(@Param("parentId") UUID parentId);
}

