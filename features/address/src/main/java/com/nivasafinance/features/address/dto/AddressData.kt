package com.nivasafinance.features.address.dto

import com.nivasafinance.features.address.entity.Address
import java.util.UUID

data class AddressData(
    val id: UUID?,
    val entityId: UUID?,
    val entityType: String?,
    val addressType: String?,
    val isPrimary: Boolean,
    val addressOne: String?,
    val addressTwo: String?,
    val landmark: String?,
    val district: String?,
    val state: String?,
    val pincode: String,
    val addressSource: String?
) {
    companion object {
        fun fromEntity(address: Address): AddressData {
            return AddressData(
                id = address.id,
                entityId = address.entityId,
                entityType = address.entityType,
                addressType = address.addressType,
                isPrimary = address.isPrimary,
                addressOne = address.addressOne,
                addressTwo = address.addressTwo,
                landmark = address.landmark,
                district = address.district,
                state = address.state,
                pincode = address.pincode,
                addressSource = address.addressSource
            )
        }
    }
}
