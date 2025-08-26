package com.nivasafinance.features.master.pincode.dto

import com.nivasafinance.features.master.pincode.entity.Pincode
import java.util.UUID

data class PincodeData(
    val id: UUID?,
    val pincode: String,
    val area: String,
    val district: String?,
    val country: String?,
    val isServicable: Boolean
) {
    companion object {
        fun fromEntity(entity: Pincode): PincodeData {
            return PincodeData(
                id = entity.id,
                pincode = entity.pincode,
                area = entity.area,
                district = entity.district,
                country = entity.country,
                isServicable = entity.isServicable
            )
        }
    }
}
