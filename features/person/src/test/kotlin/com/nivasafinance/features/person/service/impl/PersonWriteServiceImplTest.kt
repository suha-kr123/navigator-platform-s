package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.service.AddressWriteService
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.AddressDetails
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.exception.AddressTypeAlreadyExistsException
import com.nivasafinance.features.address.exception.AddressTypeNotFoundException
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class PersonWriteServiceImplTest {

    private val personRepository = mockk<PersonRepository>()
    private val personReadService = mockk<PersonReadService>()
    private val addressWriteService = mockk<AddressWriteService>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var personWriteService: PersonWriteServiceImpl

    private val personId = UUID.randomUUID()
    private val addressId = UUID.randomUUID()
    private val addressCreateRequest = AddressCreateRequest(
        addressOne = "123 Main St",
        addressTwo = "Apt 4B",
        landmark = "Near Park",
        district = "Mysore",
        state = "Karnataka",
        pincode = "570001",
        addressSource = "CUSTOMER"
    )

    @BeforeEach
    fun setup() {
        personWriteService = PersonWriteServiceImpl(personRepository, personReadService, addressWriteService)
        // Set up the BaseNavigatorService dependencies
        personWriteService.modelMapper = modelMapper
        personWriteService.messageSource = messageSource
    }

    @Test
    fun `addAddress should add address to person when person exists`() {
        // Given
        val addressType = "HOME"
        val existingPersonDto = PersonDto(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = null
        )
        val existingPerson = Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = null
        )
        val addressResponse = AddressResponse(
            id = addressId,
            addressOne = "123 Main St",
            addressTwo = "Apt 4B",
            landmark = "Near Park",
            district = "Mysore",
            state = "Karnataka",
            pincode = "570001",
            addressSource = "CUSTOMER"
        )
        every { addressWriteService.createAddress(addressCreateRequest) } returns addressResponse
        every { personReadService.getPerson(personId) } returns existingPersonDto
        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every { personRepository.save(any<Person>()) } returns existingPerson

        // When
        val result = personWriteService.addAddress(personId, addressCreateRequest, addressType)

        // Then
        assertEquals(addressId, result.id)
        assertEquals("123 Main St", result.addressOne)
        assertEquals("Apt 4B", result.addressTwo)

        verify(exactly = 1) { addressWriteService.createAddress(addressCreateRequest) }
        verify(exactly = 1) { personReadService.getPerson(personId) }
        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { personRepository.save(any<Person>()) }
    }

    @Test
    fun `addAddress should add address to existing addresses list`() {
        // Given
        val addressType = "OFFICE"
        val existingAddress = AddressDetails(addressId = UUID.randomUUID(), type = "HOME")
        val existingPersonDto = PersonDto(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )
        val existingPerson = Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )
        val addressResponse = AddressResponse(
            id = addressId,
            addressOne = "123 Main St",
            addressTwo = "Apt 4B",
            landmark = "Near Park",
            district = "Mysore",
            state = "Karnataka",
            pincode = "570001",
            addressSource = "CUSTOMER"
        )

        every { addressWriteService.createAddress(addressCreateRequest) } returns addressResponse
        every { personReadService.getPerson(personId) } returns existingPersonDto
        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every { personRepository.save(any<Person>()) } returns existingPerson

        // When
        val result = personWriteService.addAddress(personId, addressCreateRequest, addressType)

        // Then
        assertEquals(addressId, result.id)
        assertEquals("123 Main St", result.addressOne)
        assertEquals("Apt 4B", result.addressTwo)

        verify(exactly = 1) { addressWriteService.createAddress(addressCreateRequest) }
        verify(exactly = 1) { personReadService.getPerson(personId) }
        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { personRepository.save(any<Person>()) }
    }

    @Test
    fun `addAddress should throw exception when person not found`() {
        // Given
        val addressType = "HOME"
        every { messageSource.getMessage("error.person.id.not.found", any(), any()) } returns "Person not found"
        every { personReadService.getPerson(personId) } throws PersonNotFoundException(personId, messageSource)

        // When & Then
        assertThrows<PersonNotFoundException> {
            personWriteService.addAddress(personId, addressCreateRequest, addressType)
        }

        verify(exactly = 0) { addressWriteService.createAddress(any()) }
        verify(exactly = 1) { personReadService.getPerson(personId) }
        verify(exactly = 0) { personRepository.findById(any()) }
        verify(exactly = 0) { personRepository.save(any<Person>()) }
    }

    @Test
    fun `addAddress should throw exception when address type already exists`() {
        // Given
        val addressType = "HOME"
        val existingAddress = AddressDetails(addressId = UUID.randomUUID(), type = addressType)
        val existingPersonDto = PersonDto(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )
        val existingPerson = Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )

        every { personReadService.getPerson(personId) } returns existingPersonDto
        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every {
            messageSource.getMessage(
                any(),
                any(),
                any()
            )
        } returns "Person already has an address of type: $addressType"

        // When & Then
        assertThrows<AddressTypeAlreadyExistsException> {
            personWriteService.addAddress(personId, addressCreateRequest, addressType)
        }

        verify(exactly = 1) { personReadService.getPerson(personId) }
        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 0) { addressWriteService.createAddress(any()) }
        verify(exactly = 0) { personRepository.save(any<Person>()) }
    }

    @Test
    fun `removeAddress should remove address by addressId when found`() {
        // Given
        val testAddressId = UUID.randomUUID()
        val existingAddress = AddressDetails(addressId = testAddressId, type = "HOME")
        val existingPerson = Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )
        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every { personRepository.save(any<Person>()) } returns existingPerson
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"
        every { addressWriteService.deleteAddress(testAddressId) } returns AddressResponse(
            id = testAddressId,
            addressOne = "123 Main St",
            addressTwo = "Apt 4B",
            landmark = "Near Park",
            district = "Mysore",
            state = "Karnataka",
            pincode = "570001",
            addressSource = "CUSTOMER"
        )

        // When & Then - should not throw exception
        personWriteService.removeAddress(personId, testAddressId)

        // Verify interactions
        verify { personRepository.findById(personId) }
        verify { personRepository.save(any<Person>()) }
        verify { addressWriteService.deleteAddress(testAddressId) }
    }

    @Test
    fun `removeAddress should throw exception when address not found by addressId`() {
        // Given
        val existingAddress = AddressDetails(addressId = UUID.randomUUID(), type = "HOME")
        val existingPerson = Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )
        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every { messageSource.getMessage(any(), any(), any()) } returns "Address not found"

        // When & Then
        assertThrows<AddressNotFoundException> {
            personWriteService.removeAddress(personId, addressId)
        }

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 0) { personRepository.save(any<Person>()) }
        verify(exactly = 0) { addressWriteService.deleteAddress(any()) }
    }

    @Test
    fun `removeAddress should remove address by addressType when found`() {
        // Given
        val testAddressId = UUID.randomUUID()
        val addressType = "HOME"
        val existingAddress = AddressDetails(addressId = testAddressId, type = addressType)
        val existingPerson = Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )
        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every { personRepository.save(any<Person>()) } returns existingPerson
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"
        every { addressWriteService.deleteAddress(testAddressId) } returns AddressResponse(
            id = testAddressId,
            addressOne = "123 Main St",
            addressTwo = "Apt 4B",
            landmark = "Near Park",
            district = "Mysore",
            state = "Karnataka",
            pincode = "570001",
            addressSource = "CUSTOMER"
        )

        // When
        personWriteService.removeAddress(personId, addressType)

        // Then
        verify { personRepository.findById(personId) }
        verify { personRepository.save(any<Person>()) }
        verify { addressWriteService.deleteAddress(testAddressId) }
    }

    @Test
    fun `removeAddress should throw exception when address not found by addressType`() {
        // Given
        val addressType = "OFFICE"
        val existingAddress = AddressDetails(addressId = addressId, type = "HOME")
        val existingPerson = Person(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            addresses = listOf(existingAddress)
        )
        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every { messageSource.getMessage(any(), any(), any()) } returns "Address type not found"

        // When & Then
        assertThrows<AddressTypeNotFoundException> {
            personWriteService.removeAddress(personId, addressType)
        }

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 0) { personRepository.save(any<Person>()) }
        verify(exactly = 0) { addressWriteService.deleteAddress(any()) }
    }
}