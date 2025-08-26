package com.nivasafinance.features.person.controller

import com.nivasafinance.features.person.dto.PersonAddressMappingRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonAddressMappingUpdateRequest
import com.nivasafinance.features.person.service.PersonService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PersonAddressController Tests")
class PersonAddressControllerTest {

    private val personService = mockk<PersonService>()
    private lateinit var personAddressController: PersonAddressController

    private val personId = UUID.randomUUID()
    private val addressId = UUID.randomUUID()
    private val expectedResponse = PersonAddressMappingResponse(
        id = addressId,
        personId = personId,
        address = com.nivasafinance.features.address.dto.AddressResponse(
            id = UUID.randomUUID(),
            addressOne = "123 Main St",
            pincode = "123456"
        ),
        addressType = "HOME"
    )

    @BeforeEach
    fun setup() {
        personAddressController = PersonAddressController(personService)
    }

    @Test
    @DisplayName("should add address to person successfully")
    fun `addAddressToPerson should add address to person successfully`() {
        val request = PersonAddressMappingRequest(
            addressOne = "123 Main St",
            pincode = "123456",
            addressType = "HOME"
        )
        every { personService.addAddressToPerson(personId, request) } returns expectedResponse

        val result = personAddressController.addAddressToPerson(personId, request)

        assertNotNull(result)
        assertEquals(201, result.statusCode.value())
        assertEquals(expectedResponse, result.body)
        verify(exactly = 1) { personService.addAddressToPerson(personId, request) }
    }

    @Test
    @DisplayName("should get person addresses successfully")
    fun `getPersonAddresses should get person addresses successfully`() {
        val addresses = listOf(expectedResponse)
        every { personService.getPersonAddresses(personId) } returns addresses

        val result = personAddressController.getPersonAddresses(personId)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(addresses, result.body)
        verify(exactly = 1) { personService.getPersonAddresses(personId) }
    }

    @Test
    @DisplayName("should update person address mapping successfully")
    fun `updatePersonAddressMapping should update person address mapping successfully`() {
        val request = PersonAddressMappingUpdateRequest(
            addressType = "WORK"
        )
        every { personService.updatePersonAddressMapping(personId, addressId, request) } returns expectedResponse

        val result = personAddressController.updatePersonAddressMapping(personId, addressId, request)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedResponse, result.body)
        verify(exactly = 1) { personService.updatePersonAddressMapping(personId, addressId, request) }
    }

    @Test
    @DisplayName("should remove address from person successfully")
    fun `removeAddressFromPerson should remove address from person successfully`() {
        every { personService.removeAddressFromPerson(personId, addressId) } returns Unit

        val result = personAddressController.removeAddressFromPerson(personId, addressId)

        assertNotNull(result)
        assertEquals(204, result.statusCode.value())
        verify(exactly = 1) { personService.removeAddressFromPerson(personId, addressId) }
    }
}
