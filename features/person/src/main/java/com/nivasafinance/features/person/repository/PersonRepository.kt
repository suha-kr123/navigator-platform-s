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
}
