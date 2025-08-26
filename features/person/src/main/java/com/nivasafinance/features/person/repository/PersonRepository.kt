package com.nivasafinance.features.person.repository

import com.nivasafinance.features.person.entity.Person
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface PersonRepository : JpaRepository<Person, UUID> {
    @Query(
        "SELECT * FROM person WHERE mobile_numbers @> jsonb_build_array(" +
            "jsonb_build_object('isPrimary', true, 'number', :primaryMobileNo))",
        nativeQuery = true
    )
    fun findByPrimaryMobileNo(primaryMobileNo: String): Person?
}
