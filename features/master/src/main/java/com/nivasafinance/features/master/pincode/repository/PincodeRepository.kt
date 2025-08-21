package com.nivasafinance.features.master.pincode.repository

import com.nivasafinance.features.master.pincode.entity.PincodeEntity
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@JaversSpringDataAuditable
interface PincodeRepository : JpaRepository<PincodeEntity, UUID> {
    fun findAllByPincode(pincode: String): List<PincodeEntity>
}
