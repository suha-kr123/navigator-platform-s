package com.nivasafinance.features.person.repository;

import com.nivasafinance.features.person.entity.Person;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query(value = "SELECT * FROM n_person p WHERE LOWER(p.email) = LOWER(:email) AND p.is_deleted = false",
           nativeQuery = true)
    List<Person> findByEmailIgnoreCase(@Param("email") String email);

    @Query(value = "SELECT * FROM n_person p WHERE EXISTS " +
           "(SELECT 1 FROM jsonb_array_elements(p.mobile_numbers) AS m " +
           "WHERE m->>'number' = :mobileNumber AND (m->>'isPrimary')::boolean = true) " +
           "AND p.is_deleted = false",
           nativeQuery = true)
    Optional<Person> findByPrimaryMobileNumber(@Param("mobileNumber") String mobileNumber);

    @Query(value = "SELECT * FROM n_person p WHERE EXISTS " +
           "(SELECT 1 FROM jsonb_array_elements(p.mobile_numbers) AS m " +
           "WHERE m->>'number' = :mobileNumber) " +
           "AND p.is_deleted = false",
           nativeQuery = true)
    Optional<Person> findByMobileNumber(@Param("mobileNumber") String mobileNumber);

    @Query("SELECT p FROM Person p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Person> findByIdAndNotDeleted(@Param("id") Long id);

    // Unfiltered — used by creation flows to prevent duplicate persons
    @Query(value = "SELECT * FROM n_person p WHERE EXISTS " +
           "(SELECT 1 FROM jsonb_array_elements(p.mobile_numbers) AS m " +
           "WHERE m->>'number' = :mobileNumber AND (m->>'isPrimary')::boolean = true)",
           nativeQuery = true)
    Optional<Person> findByPrimaryMobileNumberIncludingDeleted(@Param("mobileNumber") String mobileNumber);
}

