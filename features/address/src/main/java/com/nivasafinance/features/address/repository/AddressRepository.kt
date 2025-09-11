package com.nivasafinance.features.address.repository

import com.nivasafinance.features.address.entity.Address
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
@Suppress("Indentation")
interface AddressRepository : JpaRepository<Address, UUID> {

    fun findByEntityIdAndEntityType(entityId: UUID, entityType: String): List<Address>

    fun findByEntityIdAndEntityTypeAndAddressType(
        entityId: UUID,
        entityType: String,
        addressType: String
    ): List<Address>

    fun findByEntityIdAndEntityTypeAndIsPrimary(entityId: UUID, entityType: String, isPrimary: Boolean): List<Address>

    fun existsByEntityIdAndEntityTypeAndAddressType(entityId: UUID, entityType: String, addressType: String): Boolean

    fun existsByEntityIdAndEntityTypeAndAddressTypeAndIsPrimary(
        entityId: UUID,
        entityType: String,
        addressType: String,
        isPrimary: Boolean
    ): Boolean

    @Query("SELECT a FROM Address a WHERE a.entityType = :entityType AND a.addressType = :addressType")
    fun findByEntityTypeAndAddressType(
        @Param("entityType") entityType: String,
        @Param("addressType") addressType: String
    ): List<Address>

    @Query("SELECT a FROM Address a WHERE a.entityType = :entityType AND a.isPrimary = true")
    fun findPrimaryAddressesByEntityType(@Param("entityType") entityType: String): List<Address>
}
