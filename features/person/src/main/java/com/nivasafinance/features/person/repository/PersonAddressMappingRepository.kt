package com.nivasafinance.features.person.repository

import com.nivasafinance.features.person.entity.PersonAddressMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PersonAddressMappingRepository : JpaRepository<PersonAddressMapping, UUID> {
    fun findByPersonId(personId: UUID): List<PersonAddressMapping>
    fun findByPersonIdAndAddressId(personId: UUID, addressId: UUID): PersonAddressMapping?
    fun existsByPersonIdAndAddressType(personId: UUID, addressType: String): Boolean
}
