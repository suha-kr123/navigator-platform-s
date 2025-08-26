package com.nivasafinance.features.master.pincode.repository

import com.nivasafinance.features.master.pincode.entity.Pincode
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface PincodeRepository : JpaRepository<Pincode, UUID> {
    fun findAllByPincode(pincode: String): List<Pincode>
}
