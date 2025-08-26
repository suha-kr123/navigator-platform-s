package com.nivasafinance.features.person.repository

import com.nivasafinance.features.person.entity.PersonIdentifier
import com.nivasafinance.features.person.enum.IdentifierType
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface PersonIdentifierRepository : JpaRepository<PersonIdentifier, UUID> {
    fun findByPersonId(personId: UUID): List<PersonIdentifier>
    fun findByPersonIdAndType(personId: UUID, type: IdentifierType): PersonIdentifier?
    fun existsByPersonIdAndType(personId: UUID, type: IdentifierType): Boolean
}
