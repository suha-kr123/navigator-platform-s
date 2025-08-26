package com.nivasafinance

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.master.pincode.dto.PincodeData
import java.util.UUID

object AddressTestUtils {

    fun createTestAddressEntity(
        id: UUID = UUID.randomUUID(),
        addressOne: String = "123 Main St",
        addressTwo: String? = "Apt 4B",
        landmark: String? = "Near Park",
        district: String = "Mysore",
        state: String = "Karnataka",
        pincode: String = "570001",
        addressSource: String = "CUSTOMER"
    ): Address {
        return Address(
            id = id,
            addressOne = addressOne,
            addressTwo = addressTwo,
            landmark = landmark,
            district = district,
            state = state,
            pincode = pincode,
            addressSource = addressSource
        )
    }

    fun createTestAddressCreateRequest(
        addressOne: String = "123 Main St",
        addressTwo: String? = "Apt 4B",
        landmark: String? = "Near Park",
        district: String = "Mysore",
        state: String = "Karnataka",
        pincode: String = "570001",
        addressSource: String = "CUSTOMER"
    ): AddressCreateRequest {
        return AddressCreateRequest(
            addressOne = addressOne,
            addressTwo = addressTwo,
            landmark = landmark,
            district = district,
            state = state,
            pincode = pincode,
            addressSource = addressSource
        )
    }

    fun createTestAddressUpdateRequest(
        addressOne: String = "456 Oak St",
        addressTwo: String? = "Unit 7C",
        landmark: String? = "Near Mall",
        district: String = "Bangalore",
        state: String = "Karnataka",
        pincode: String = "560001",
        addressSource: String = "ADVISOR"
    ): AddressUpdateRequest {
        return AddressUpdateRequest(
            addressOne = addressOne,
            addressTwo = addressTwo,
            landmark = landmark,
            district = district,
            state = state,
            pincode = pincode,
            addressSource = addressSource
        )
    }

    fun createTestAddressResponse(
        id: UUID = UUID.randomUUID(),
        addressOne: String = "123 Main St",
        addressTwo: String? = "Apt 4B",
        landmark: String? = "Near Park",
        district: String = "Mysore",
        state: String = "Karnataka",
        pincode: String = "570001",
        addressSource: String = "CUSTOMER"
    ): AddressResponse {
        return AddressResponse(
            id = id,
            addressOne = addressOne,
            addressTwo = addressTwo,
            landmark = landmark,
            district = district,
            state = state,
            pincode = pincode,
            addressSource = addressSource
        )
    }

    fun createTestPincodeData(
        pincode: String = "570001",
        area: String = "Mysore Area",
        district: String? = "Mysore",
        country: String? = "India",
        isServicable: Boolean = true
    ): PincodeData {
        return PincodeData(
            id = UUID.randomUUID(),
            pincode = pincode,
            area = area,
            district = district,
            country = country,
            isServicable = isServicable
        )
    }
}
