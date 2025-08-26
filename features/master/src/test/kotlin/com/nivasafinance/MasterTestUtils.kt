package com.nivasafinance

import com.nivasafinance.features.master.pincode.dto.PincodeData
import com.nivasafinance.features.master.pincode.dto.PincodeResponse
import com.nivasafinance.features.master.pincode.entity.Pincode
import java.util.UUID

object MasterTestUtils {

    fun createTestPincodeEntity(
        pincode: String = "123456",
        area: String = "Mumbai Area",
        district: String? = "Mumbai",
        country: String? = "India",
        isServicable: Boolean = true
    ): Pincode {
        return Pincode(
            id = UUID.randomUUID(),
            pincode = pincode,
            area = area,
            district = district,
            country = country,
            isServicable = isServicable
        )
    }

    fun createTestPincodeData(
        pincode: String = "123456",
        area: String = "Mumbai Area",
        district: String? = "Mumbai",
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

    fun createTestPincodeResponse(
        pincode: String = "123456",
        areas: List<String> = listOf("Mumbai Area"),
        district: String? = "Mumbai",
        country: String? = "India",
        isServicable: Boolean = true
    ): PincodeResponse {
        return PincodeResponse(
            pincode = pincode,
            areas = areas,
            district = district,
            country = country,
            isServicable = isServicable
        )
    }
}
