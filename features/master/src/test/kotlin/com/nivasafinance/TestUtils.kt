package com.nivasafinance

import com.nivasafinance.features.master.pincode.dto.PincodeResponseDto
import com.nivasafinance.features.master.pincode.entity.PincodeEntity
import java.util.UUID

object TestUtils {

    fun createTestPincodeEntity(
        pincode: String = "123456",
        area: String = "Mumbai Area",
        district: String? = "Mumbai",
        country: String? = "India",
        isServicable: Boolean = true
    ): PincodeEntity {
        return PincodeEntity(
            id = UUID.randomUUID(),
            pincode = pincode,
            area = area,
            district = district,
            country = country,
            isServicable = isServicable
        )
    }

    fun createTestPincodeResponseDto(
        pincode: String = "123456",
        areas: List<String> = listOf("Mumbai Area"),
        district: String? = "Mumbai",
        country: String? = "India",
        isServicable: Boolean = true
    ): PincodeResponseDto {
        return PincodeResponseDto(
            pincode = pincode,
            areas = areas,
            district = district,
            country = country,
            isServicable = isServicable
        )
    }
}
