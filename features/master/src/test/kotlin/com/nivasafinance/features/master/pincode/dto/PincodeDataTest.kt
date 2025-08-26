package com.nivasafinance.features.master.pincode.dto

import com.nivasafinance.MasterTestUtils.createTestPincodeEntity
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PincodeData Tests")
class PincodeDataTest {

    @Test
    @DisplayName("fromEntity should map entity to data correctly")
    fun `fromEntity should map entity to data correctly`() {
        // Given
        val entity = createTestPincodeEntity(
            pincode = "123456",
            area = "Test Area",
            district = "Test District",
            country = "Test Country",
            isServicable = true
        )

        // When
        val result = PincodeData.fromEntity(entity)

        // Then
        assertNotNull(result)
        assertEquals(entity.id, result.id)
        assertEquals(entity.pincode, result.pincode)
        assertEquals(entity.area, result.area)
        assertEquals(entity.district, result.district)
        assertEquals(entity.country, result.country)
        assertEquals(entity.isServicable, result.isServicable)
    }

    @Test
    @DisplayName("fromEntity should handle null values correctly")
    fun `fromEntity should handle null values correctly`() {
        // Given
        val entity = createTestPincodeEntity(
            pincode = "123456",
            area = "Test Area",
            district = null,
            country = null,
            isServicable = false
        )

        // When
        val result = PincodeData.fromEntity(entity)

        // Then
        assertNotNull(result)
        assertEquals(entity.id, result.id)
        assertEquals(entity.pincode, result.pincode)
        assertEquals(entity.area, result.area)
        assertEquals(null, result.district)
        assertEquals(null, result.country)
        assertEquals(false, result.isServicable)
    }
}
