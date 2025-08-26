package com.nivasafinance.features.master.pincode.controller

import com.nivasafinance.MasterTestUtils.createTestPincodeResponse
import com.nivasafinance.features.master.pincode.service.PincodeService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PincodeController Tests")
class PincodeControllerTest {

    private val pincodeService = mockk<PincodeService>()
    private lateinit var pincodeController: PincodeController

    private val pincode = "123456"
    private val expectedDto = createTestPincodeResponse(pincode = pincode)

    @BeforeEach
    fun setup() {
        pincodeController = PincodeController(pincodeService)
    }

    @Test
    @DisplayName("getPincodeDetails should return pincode details")
    fun `getPincodeDetails should return pincode details`() {
        // Given
        every { pincodeService.getByPincode(pincode) } returns expectedDto

        // When
        val result = pincodeController.getPincodeDetails(pincode)

        // Then
        assertNotNull(result)
        assertEquals(expectedDto.pincode, result.pincode)
        assertEquals(expectedDto.areas, result.areas)
        assertEquals(expectedDto.district, result.district)
        assertEquals(expectedDto.country, result.country)
        assertEquals(expectedDto.isServicable, result.isServicable)

        verify(exactly = 1) { pincodeService.getByPincode(pincode) }
    }
}
