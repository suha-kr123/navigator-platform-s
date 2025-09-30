package com.nivasafinance.features.person.repository

import com.nivasafinance.features.person.entity.Person
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface PersonRepository : JpaRepository<Person, UUID> {

    @Query(
        value = "SELECT * FROM person WHERE mobile_numbers::text LIKE %:mobileNumber%",
        nativeQuery = true
    )
    fun findByMobileNumber(@Param("mobileNumber") mobileNumber: String): List<Person>

    @Query(
        value = "SELECT * FROM person WHERE mobile_numbers::text LIKE %:mobileNumber% AND id != :excludeId",
        nativeQuery = true
    )
    fun findByMobileNumberExcludingId(
        @Param("mobileNumber") mobileNumber: String,
        @Param("excludeId") excludeId: UUID
    ): List<Person>
}
