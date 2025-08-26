package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.exception.AddressNotFoundException
import com.nivasafinance.features.address.exception.AddressTypeAlreadyExistsException
import com.nivasafinance.features.address.exception.AddressTypeNotFoundException
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonIdentifierCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.entity.PersonAddressMapping
import com.nivasafinance.features.person.entity.PersonIdentifier
import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.enum.IdentifierType
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.exception.DuplicateIdentifierTypeException
import com.nivasafinance.features.person.exception.PersonIdentifierNotFoundException
import com.nivasafinance.features.person.exception.InvalidIdentifierTypeException
import com.nivasafinance.features.person.exception.InvalidMobileNumberException
import com.nivasafinance.features.person.exception.DuplicatePrimaryMobileNumberException
import com.nivasafinance.features.person.exception.PrimaryMobileNumberAlreadyExistsException
import com.nivasafinance.features.person.repository.PersonRepository
import com.nivasafinance.features.person.repository.PersonAddressMappingRepository
import com.nivasafinance.features.person.repository.PersonIdentifierRepository
import data.enums.Gender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class PersonWriteServiceImplTest {

    private val personRepository = mockk<PersonRepository>()
    private val personAddressMappingRepository = mockk<PersonAddressMappingRepository>()
    private val personIdentifierRepository = mockk<PersonIdentifierRepository>()
    private val addressService = mockk<AddressService>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var personWriteService: PersonWriteServiceImpl

    private val personId = UUID.randomUUID()
    private val addressId = UUID.randomUUID()
    private val identifierId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        personWriteService = PersonWriteServiceImpl(
            personRepository,
            personAddressMappingRepository,
            personIdentifierRepository,
            addressService
        )

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = personWriteService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(personWriteService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"
    }

    @Nested
    @DisplayName("createPerson Tests")
    inner class CreatePersonTests {

        @Test
        fun `createPerson should create person successfully`() {
            // Given
            val request = PersonCreateRequest(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    MobileNumberDetails(number = "9876543210", isPrimary = true)
                ),
                email = "john.doe@example.com",
                dateOfBirth = LocalDate.of(1990, 1, 1),
                gender = Gender.MALE
            )

            val savedPerson = Person(
                id = personId,
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    MobileNumberDetails(number = "9876543210", isPrimary = true)
                ),
                email = "john.doe@example.com",
                dateOfBirth = LocalDate.of(1990, 1, 1),
                gender = Gender.MALE
            )

            every { personRepository.findByPrimaryMobileNo("9876543210") } returns null
            every { personRepository.save(any<Person>()) } returns savedPerson

            // When
            val result = personWriteService.createPerson(request)

            // Then
            assertEquals(personId, result)
            verify(exactly = 1) { personRepository.findByPrimaryMobileNo("9876543210") }
            verify(exactly = 1) { personRepository.save(any<Person>()) }
        }

        @Test
        fun `createPerson should throw exception when primary mobile number already exists`() {
            // Given
            val request = PersonCreateRequest(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    MobileNumberDetails(number = "9876543210", isPrimary = true)
                )
            )

            val existingPerson = Person(id = UUID.randomUUID(), firstName = "Jane", lastName = "Doe")
            every { personRepository.findByPrimaryMobileNo("9876543210") } returns existingPerson

            // When & Then
            assertThrows<PrimaryMobileNumberAlreadyExistsException> {
                personWriteService.createPerson(request)
            }
        }

        @Test
        fun `createPerson should throw exception when invalid mobile number`() {
            // Given
            val request = PersonCreateRequest(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    MobileNumberDetails(number = "123", isPrimary = true)
                )
            )

            // When & Then
            assertThrows<InvalidMobileNumberException> {
                personWriteService.createPerson(request)
            }
        }
    }

    @Nested
    @DisplayName("deletePerson Tests")
    inner class DeletePersonTests {

        @Test
        fun `deletePerson should delete person successfully`() {
            // Given
            every { personRepository.existsById(personId) } returns true
            every { personRepository.deleteById(personId) } returns Unit

            // When
            personWriteService.deletePerson(personId)

            // Then
            verify(exactly = 1) { personRepository.existsById(personId) }
            verify(exactly = 1) { personRepository.deleteById(personId) }
        }

        @Test
        fun `deletePerson should throw exception when person not found`() {
            // Given
            every { personRepository.existsById(personId) } returns false

            // When & Then
            assertThrows<PersonNotFoundException> {
                personWriteService.deletePerson(personId)
            }
        }
    }

    @Nested
    @DisplayName("updatePerson Tests")
    inner class UpdatePersonTests {

        @Test
        fun `updatePerson should update person successfully`() {
            // Given
            val request = PersonUpdateRequest(
                firstName = "Jane",
                lastName = "Smith"
            )

            val existingPerson = Person(
                id = personId,
                firstName = "John",
                lastName = "Doe"
            )

            every { personRepository.findById(personId) } returns Optional.of(existingPerson)
            every { personRepository.save(any<Person>()) } returns existingPerson

            // When
            personWriteService.updatePerson(personId, request)

            // Then
            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 1) { personRepository.save(any<Person>()) }
        }

        @Test
        fun `updatePerson should throw exception when person not found`() {
            // Given
            val request = PersonUpdateRequest(firstName = "Jane")
            every { personRepository.findById(personId) } returns Optional.empty()

            // When & Then
            assertThrows<PersonNotFoundException> {
                personWriteService.updatePerson(personId, request)
            }
        }
    }

    @Nested
    @DisplayName("addAddressToPerson Tests")
    inner class AddAddressToPersonTests {

        @Test
        fun `addAddressToPerson should add address successfully`() {
            // Given
            val request = PersonAddressMappingRequest(
                addressOne = "123 Main St",
                addressTwo = "Apt 4B",
                landmark = "Near Park",
                district = "Mysore",
                state = "Karnataka",
                pincode = "570001",
                addressType = "HOME",
                addressSource = "CUSTOMER"
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

            val savedMapping = PersonAddressMapping(
                id = UUID.randomUUID(),
                personId = personId,
                addressId = addressId,
                addressType = "HOME"
            )

            every { personRepository.existsById(personId) } returns true
            every { personAddressMappingRepository.existsByPersonIdAndAddressType(personId, "HOME") } returns false
            every { addressService.createAddress(any<AddressCreateRequest>()) } returns addressResponse
            every { personAddressMappingRepository.save(any<PersonAddressMapping>()) } returns savedMapping

            // When
            val result = personWriteService.addAddressToPerson(personId, request)

            // Then
            assertEquals(personId, result.personId)
            assertEquals(addressId, result.address.id)
            assertEquals("HOME", result.addressType)
            verify(exactly = 1) { personRepository.existsById(personId) }
            verify(exactly = 1) { personAddressMappingRepository.existsByPersonIdAndAddressType(personId, "HOME") }
            verify(exactly = 1) { addressService.createAddress(any<AddressCreateRequest>()) }
            verify(exactly = 1) { personAddressMappingRepository.save(any<PersonAddressMapping>()) }
        }

        @Test
        fun `addAddressToPerson should throw exception when person not found`() {
            // Given
            val request = PersonAddressMappingRequest(
                addressOne = "123 Main St",
                pincode = "570001",
                addressType = "HOME"
            )

            every { personRepository.existsById(personId) } returns false

            // When & Then
            assertThrows<PersonNotFoundException> {
                personWriteService.addAddressToPerson(personId, request)
            }
        }
    }

    @Nested
    @DisplayName("createPersonIdentifier Tests")
    inner class CreatePersonIdentifierTests {

        @Test
        fun `createPersonIdentifier should create identifier successfully`() {
            // Given
            val request = PersonIdentifierCreateRequest(
                identifier = "123456789012",
                type = IdentifierType.AADHAR
            )

            val savedIdentifier = PersonIdentifier(
                id = identifierId,
                personId = personId,
                identifier = "123456789012",
                type = IdentifierType.AADHAR
            )

            every { personRepository.existsById(personId) } returns true
            every { personIdentifierRepository.existsByPersonIdAndType(personId, IdentifierType.AADHAR) } returns false
            every { personIdentifierRepository.save(any<PersonIdentifier>()) } returns savedIdentifier

            // When
            val result = personWriteService.createPersonIdentifier(personId, request)

            // Then
            assertEquals(identifierId, result.id)
            assertEquals(IdentifierType.AADHAR, result.type)
            assertEquals("123456789012", result.identifier)
            verify(exactly = 1) { personRepository.existsById(personId) }
            verify(exactly = 1) { personIdentifierRepository.existsByPersonIdAndType(personId, IdentifierType.AADHAR) }
            verify(exactly = 1) { personIdentifierRepository.save(any<PersonIdentifier>()) }
        }

        @Test
        fun `createPersonIdentifier should throw exception when person not found`() {
            // Given
            val request = PersonIdentifierCreateRequest(
                identifier = "123456789012",
                type = IdentifierType.AADHAR
            )

            every { personRepository.existsById(personId) } returns false

            // When & Then
            assertThrows<PersonNotFoundException> {
                personWriteService.createPersonIdentifier(personId, request)
            }
        }
    }
}
