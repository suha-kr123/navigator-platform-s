package com.nivasafinance.features.address.dto

import com.nivasafinance.features.address.entity.Address
import java.util.UUID

data class AddressData(
    val id: UUID?,
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
