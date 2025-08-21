package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.exception.AddressTypeAlreadyExistsException
import com.nivasafinance.features.address.exception.AddressTypeNotFoundException
import com.nivasafinance.features.address.service.AddressWriteService
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.AddressDetails
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.service.PersonReadService
import data.Identifier
import data.IdentifierType
import exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
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

        // Mock modelMapper for BaseNavigatorService using reflection
        val modelMapperField = personWriteService.javaClass.superclass.getDeclaredField("modelMapper")
        modelMapperField.isAccessible = true
        modelMapperField.set(personWriteService, modelMapper)

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = personWriteService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(personWriteService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"
    }

    @Nested
    @DisplayName("addAddress Tests")
    inner class AddAddressTests {

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
    }

    @Nested
    @DisplayName("removeAddress Tests")
    inner class RemoveAddressTests {

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
            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 1) { personRepository.save(any<Person>()) }
            verify(exactly = 1) { addressWriteService.deleteAddress(testAddressId) }
        }

        @Test
        fun `removeAddress should throw exception when address type not found`() {
            // Given
            val addressType = "OFFICE"
            val existingAddress = AddressDetails(addressId = UUID.randomUUID(), type = "HOME")
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

    @Nested
    @DisplayName("addIdentifier Tests")
    inner class AddIdentifierTests {

        @Test
        fun `addIdentifier should add identifier to person with no existing identifiers`() {
            val identifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "ABCDE1234F",
                type = IdentifierType.PAN
            )
            val person = Person(id = personId, firstName = "John", lastName = "Doe")
            person.identifiers = null

            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(person) } returns person

            personWriteService.addIdentifier(personId, identifier)

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 1) { personRepository.save(person) }
            assertEquals(1, person.identifiers?.size)
            assertEquals(identifier, person.identifiers?.first())
        }

        @Test
        fun `addIdentifier should add identifier to person with existing identifiers`() {
            val existingIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "VOT123456789",
                type = IdentifierType.VOTER
            )
            val newIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "ABCDE1234F",
                type = IdentifierType.PAN
            )
            val person = Person(id = personId, firstName = "John", lastName = "Doe")
            person.identifiers = listOf(existingIdentifier)

            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(person) } returns person

            personWriteService.addIdentifier(personId, newIdentifier)

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 1) { personRepository.save(person) }
            assertEquals(2, person.identifiers?.size)
            assert(person.identifiers?.contains(existingIdentifier) == true)
            assert(person.identifiers?.contains(newIdentifier) == true)
        }

        @Test
        fun `addIdentifier should throw ResourceNotFoundException when person not found`() {
            val identifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "ABCDE1234F",
                type = IdentifierType.PAN
            )

            every { personRepository.findById(personId) } returns Optional.empty()

            assertThrows<ResourceNotFoundException> {
                personWriteService.addIdentifier(personId, identifier)
            }

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 0) { personRepository.save(any()) }
        }

        @Test
        fun `addIdentifier should work with all identifier types`() {
            val identifierTypes = listOf(IdentifierType.PAN, IdentifierType.VOTER)

            identifierTypes.forEach { type ->
                val identifier = Identifier(
                    id = UUID.randomUUID().toString(),
                    identifier = "TEST123",
                    type = type
                )
                val person = Person(id = personId, firstName = "John", lastName = "Doe")
                person.identifiers = emptyList()

                every { personRepository.findById(personId) } returns Optional.of(person)
                every { personRepository.save(person) } returns person

                personWriteService.addIdentifier(personId, identifier)

                assertEquals(1, person.identifiers?.size)
                assertEquals(identifier, person.identifiers?.first())
            }
        }
    }

    @Nested
    @DisplayName("updateIdentifier Tests")
    inner class UpdateIdentifierTests {

        @Test
        fun `updateIdentifier should update existing identifier successfully`() {
            val identifierId = UUID.randomUUID()
            val existingIdentifier = Identifier(
                id = identifierId.toString(),
                identifier = "OLD123",
                type = IdentifierType.PAN
            )
            val updatedIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "NEW456",
                type = IdentifierType.VOTER
            )
            val person = Person(id = personId, firstName = "John", lastName = "Doe")
            person.identifiers = listOf(existingIdentifier)

            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(person) } returns person

            personWriteService.updateIdentifier(personId, identifierId, updatedIdentifier)

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 1) { personRepository.save(person) }

            val updated = person.identifiers?.find { it.id == identifierId.toString() }
            assertEquals(updatedIdentifier.identifier, updated?.identifier)
            assertEquals(updatedIdentifier.type, updated?.type)
        }

        @Test
        fun `updateIdentifier should throw ResourceNotFoundException when person not found`() {
            val identifierId = UUID.randomUUID()
            val updatedIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "NEW456",
                type = IdentifierType.PAN
            )

            every { personRepository.findById(personId) } returns Optional.empty()

            assertThrows<ResourceNotFoundException> {
                personWriteService.updateIdentifier(personId, identifierId, updatedIdentifier)
            }

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 0) { personRepository.save(any()) }
        }

        @Test
        fun `updateIdentifier should throw ResourceNotFoundException when identifier not found`() {
            val identifierId = UUID.randomUUID()
            val existingIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "EXISTING123",
                type = IdentifierType.PAN
            )
            val updatedIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "NEW456",
                type = IdentifierType.VOTER
            )
            val person = Person(id = personId, firstName = "John", lastName = "Doe")
            person.identifiers = listOf(existingIdentifier)

            every { personRepository.findById(personId) } returns Optional.of(person)

            assertThrows<ResourceNotFoundException> {
                personWriteService.updateIdentifier(personId, identifierId, updatedIdentifier)
            }

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 0) { personRepository.save(any()) }
        }

        @Test
        fun `updateIdentifier should work with all identifier types`() {
            val identifierTypes = listOf(IdentifierType.PAN, IdentifierType.VOTER)

            identifierTypes.forEach { type ->
                val identifierId = UUID.randomUUID()
                val existingIdentifier = Identifier(
                    id = identifierId.toString(),
                    identifier = "OLD123",
                    type = type
                )
                val updatedIdentifier = Identifier(
                    id = UUID.randomUUID().toString(),
                    identifier = "NEW456",
                    type = type
                )
                val person = Person(id = personId, firstName = "John", lastName = "Doe")
                person.identifiers = listOf(existingIdentifier)

                every { personRepository.findById(personId) } returns Optional.of(person)
                every { personRepository.save(person) } returns person

                personWriteService.updateIdentifier(personId, identifierId, updatedIdentifier)

                val updated = person.identifiers?.find { it.id == identifierId.toString() }
                assertEquals(updatedIdentifier.identifier, updated?.identifier)
                assertEquals(updatedIdentifier.type, updated?.type)
            }
        }

        @Test
        fun `updateIdentifier should handle person with null identifiers list`() {
            val identifierId = UUID.randomUUID()
            val updatedIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "NEW456",
                type = IdentifierType.PAN
            )
            val person = Person(id = personId, firstName = "John", lastName = "Doe")
            person.identifiers = null

            every { personRepository.findById(personId) } returns Optional.of(person)

            assertThrows<ResourceNotFoundException> {
                personWriteService.updateIdentifier(personId, identifierId, updatedIdentifier)
            }

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 0) { personRepository.save(any()) }
        }

        @Test
        fun `updateIdentifier should handle person with empty identifiers list`() {
            val identifierId = UUID.randomUUID()
            val updatedIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "NEW456",
                type = IdentifierType.PAN
            )
            val person = Person(id = personId, firstName = "John", lastName = "Doe")
            person.identifiers = emptyList()

            every { personRepository.findById(personId) } returns Optional.of(person)

            assertThrows<ResourceNotFoundException> {
                personWriteService.updateIdentifier(personId, identifierId, updatedIdentifier)
            }

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 0) { personRepository.save(any()) }
        }
    }
}
