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
}
