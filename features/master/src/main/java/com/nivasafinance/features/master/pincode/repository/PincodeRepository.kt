package com.nivasafinance.features.master.pincode.repository

import com.nivasafinance.features.master.pincode.entity.PincodeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PincodeRepository : JpaRepository<PincodeEntity, UUID> {
    fun findAllByPincode(pincode: String): List<PincodeEntity>
}
