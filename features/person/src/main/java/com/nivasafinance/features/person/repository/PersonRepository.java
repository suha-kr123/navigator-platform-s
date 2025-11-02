package com.nivasafinance.features.person.repository;

import com.nivasafinance.features.person.entity.Person;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@JaversSpringDataAuditable
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    @Query(value = "SELECT * FROM n_person p WHERE EXISTS " +
           "(SELECT 1 FROM jsonb_array_elements(p.mobile_numbers) AS m " +
           "WHERE m->>'number' = :mobileNumber AND (m->>'isPrimary')::boolean = true)",
           nativeQuery = true)
    Optional<Person> findByPrimaryMobileNumber(@Param("mobileNumber") String mobileNumber);
}

