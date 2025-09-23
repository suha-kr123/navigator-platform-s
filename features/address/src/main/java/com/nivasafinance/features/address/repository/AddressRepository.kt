package com.nivasafinance.features.address.repository

import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.enum.AddressType
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
@Suppress("Indentation")
interface AddressRepository : JpaRepository<Address, UUID> {
    fun findByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<Address>
    fun findByEntityTypeAndEntityIdAndAddressType(
        entityType: String,
        entityId: UUID,
        addressType: AddressType
    ): Address?
    fun findByEntityTypeAndEntityIdAndId(
        entityType: String,
        entityId: UUID,
        id: UUID
    ): Address?
}
