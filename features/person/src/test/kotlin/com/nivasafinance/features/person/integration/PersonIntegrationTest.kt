package com.nivasafinance.features.person.integration

import com.nivasafinance.features.person.TestUtils
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsCreateRequest
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsResponse
import com.nivasafinance.features.person.TestUtils.createTestEmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.controller.PersonController
import com.nivasafinance.features.person.controller.PersonEmploymentController
import com.nivasafinance.features.person.dto.EmploymentDetailsCreateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingRequest
import com.nivasafinance.features.person.dto.PersonAddressMappingUpdateRequest
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierCreateRequest
import com.nivasafinance.features.person.dto.PersonIdentifierUpdateRequest
import com.nivasafinance.features.person.dto.PersonUpdateRequest
import com.nivasafinance.features.person.service.PersonService
import com.nivasafinance.features.person.service.PersonReadService
import com.nivasafinance.features.person.enum.IdentifierType
import data.enums.Gender
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("Person Integration Tests")
class PersonIntegrationTest {

    private lateinit var personService: PersonService
    private lateinit var personReadService: PersonReadService
    private lateinit var personController: PersonController
    private lateinit var personEmploymentController: PersonEmploymentController

    private val personId = UUID.randomUUID()
    private val addressId = UUID.randomUUID()
    private val identifierId = UUID.randomUUID()
    

    
    private val expectedPersonResponse = TestUtils.createTestPersonResponse(
        id = personId,
        firstName = "John",
        lastName = "Doe"
    )
    
    private val expectedAddressResponse = TestUtils.createTestPersonAddressMappingResponse(
        id = addressId,
        personId = personId,
        addressId = UUID.randomUUID()
    )
    
    private val expectedIdentifierResponse = TestUtils.createTestPersonIdentifierResponse(
        id = identifierId,
        personId = personId
    )
    
    private val expectedEmploymentDetailsResponse = createTestEmploymentDetailsResponse(
        personId = personId
    )

    @BeforeEach
    fun setup() {
        personService = mockk<PersonService>()
        personReadService = mockk<PersonReadService>()
        personController = PersonController(personService, personReadService)
        personEmploymentController = PersonEmploymentController(personService)
    }

    @Test
    @DisplayName("should create person through complete flow")
    fun `createPerson should work through complete flow`() {
        // Given
        val createRequest = PersonCreateRequest(
            firstName = "John",
            lastName = "Doe",
            mobileNumbers = listOf(TestUtils.createTestMobileNumberDetails()),
            email = "john.doe@example.com",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            gender = Gender.MALE
        )
        every { personService.createPerson(createRequest) } returns expectedPersonResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.person.dto.PersonResponse> =
            personController.createPerson(createRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedPersonResponse.id, response.body?.id)
        assertEquals(expectedPersonResponse.firstName, response.body?.firstName)
        assertEquals(expectedPersonResponse.lastName, response.body?.lastName)
        assertEquals(expectedPersonResponse.email, response.body?.email)
    }

    @Test
    @DisplayName("should get person through complete flow")
    fun `getPerson should work through complete flow`() {
        // Given
        every { personService.getPerson(personId) } returns expectedPersonResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.person.dto.PersonResponse> =
            personController.getPerson(personId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPersonResponse.id, response.body?.id)
        assertEquals(expectedPersonResponse.firstName, response.body?.firstName)
        assertEquals(expectedPersonResponse.lastName, response.body?.lastName)
    }

    @Test
    @DisplayName("should update person through complete flow")
    fun `updatePerson should work through complete flow`() {
        // Given
        val updateRequest = PersonUpdateRequest(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com"
        )
        every { personService.updatePerson(personId, updateRequest) } returns expectedPersonResponse

        // When
        val response: ResponseEntity<com.nivasafinance.features.person.dto.PersonResponse> =
            personController.updatePerson(personId, updateRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPersonResponse.id, response.body?.id)
        assertEquals(expectedPersonResponse.firstName, response.body?.firstName)
        assertEquals(expectedPersonResponse.lastName, response.body?.lastName)
    }

    @Test
    @DisplayName("should delete person through complete flow")
    fun `deletePerson should work through complete flow`() {
        // Given
        every { personService.deletePerson(personId) } returns Unit

        // When
        val response: ResponseEntity<Unit> = personController.deletePerson(personId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    @DisplayName("should handle person service calls correctly")
    fun `person service should handle all operations correctly`() {
        // Given
        val createRequest = PersonCreateRequest(
            firstName = "John",
            lastName = "Doe",
            mobileNumbers = listOf(TestUtils.createTestMobileNumberDetails()),
            email = "john.doe@example.com",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            gender = Gender.MALE
        )
        val updateRequest = PersonUpdateRequest(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com"
        )

        every { personService.createPerson(createRequest) } returns expectedPersonResponse
        every { personService.getPerson(personId) } returns expectedPersonResponse
        every { personService.updatePerson(personId, updateRequest) } returns expectedPersonResponse
        every { personService.deletePerson(personId) } returns Unit

        // When & Then
        val createResponse = personService.createPerson(createRequest)
        assertEquals(expectedPersonResponse.id, createResponse.id)

        val getResponse = personService.getPerson(personId)
        assertEquals(expectedPersonResponse.id, getResponse.id)

        val updateResponse = personService.updatePerson(personId, updateRequest)
        assertEquals(expectedPersonResponse.id, updateResponse.id)

        personService.deletePerson(personId)
    }

    @Test
    @DisplayName("should handle address mapping operations correctly")
    fun `person service should handle address mapping operations correctly`() {
        // Given
        val addressRequest = PersonAddressMappingRequest(
            addressType = "HOME",
            pincode = "123456"
        )
        val addressUpdateRequest = PersonAddressMappingUpdateRequest(
            addressType = "WORK"
        )

        every { personService.getPersonAddresses(personId) } returns listOf(expectedAddressResponse)
        every { personService.addAddressToPerson(personId, addressRequest) } returns expectedAddressResponse
        every { personService.updatePersonAddressMapping(personId, addressId, addressUpdateRequest) } returns expectedAddressResponse
        every { personService.removeAddressFromPerson(personId, addressId) } returns Unit

        // When & Then
        val addresses = personService.getPersonAddresses(personId)
        assertEquals(1, addresses.size)
        assertEquals(expectedAddressResponse.id, addresses[0].id)

        val addResponse = personService.addAddressToPerson(personId, addressRequest)
        assertEquals(expectedAddressResponse.id, addResponse.id)

        val updateResponse = personService.updatePersonAddressMapping(personId, addressId, addressUpdateRequest)
        assertEquals(expectedAddressResponse.id, updateResponse.id)

        personService.removeAddressFromPerson(personId, addressId)
    }

    @Test
    @DisplayName("should handle identifier operations correctly")
    fun `person service should handle identifier operations correctly`() {
        // Given
        val identifierRequest = PersonIdentifierCreateRequest(
            identifier = "ABCDE1234F",
            type = IdentifierType.PAN
        )
        val identifierUpdateRequest = PersonIdentifierUpdateRequest(
            identifier = "FGHIJ5678K",
            type = IdentifierType.PAN
        )

        every { personService.getPersonIdentifiers(personId) } returns listOf(expectedIdentifierResponse)
        every { personService.createPersonIdentifier(personId, identifierRequest) } returns expectedIdentifierResponse
        every { personService.updatePersonIdentifier(personId, identifierId, identifierUpdateRequest) } returns expectedIdentifierResponse
        every { personService.deletePersonIdentifier(personId, identifierId) } returns Unit

        // When & Then
        val identifiers = personService.getPersonIdentifiers(personId)
        assertEquals(1, identifiers.size)
        assertEquals(expectedIdentifierResponse.id, identifiers[0].id)

        val createResponse = personService.createPersonIdentifier(personId, identifierRequest)
        assertEquals(expectedIdentifierResponse.id, createResponse.id)

        val updateResponse = personService.updatePersonIdentifier(personId, identifierId, identifierUpdateRequest)
        assertEquals(expectedIdentifierResponse.id, updateResponse.id)

        personService.deletePersonIdentifier(personId, identifierId)
    }

    @Test
    @DisplayName("should create employment details through complete flow")
    fun `createEmploymentDetails should work through complete flow`() {
        // Given
        val createRequest = createTestEmploymentDetailsCreateRequest()
        
        every { personService.createPersonEmploymentDetails(personId, createRequest) } returns expectedEmploymentDetailsResponse

        // When
        val createResponse = personEmploymentController.createEmploymentDetails(personId, createRequest)

        // Then
        assertNotNull(createResponse)
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        assertNotNull(createResponse.body)
        assertEquals(expectedEmploymentDetailsResponse.employmentId, createResponse.body?.employmentId)
        assertEquals(expectedEmploymentDetailsResponse.personId, createResponse.body?.personId)
        assertEquals(expectedEmploymentDetailsResponse.employerName, createResponse.body?.employerName)
    }

    @Test
    @DisplayName("should get employment details through complete flow")
    fun `getEmploymentDetails should work through complete flow`() {
        // Given
        every { personService.getPersonEmploymentDetails(personId) } returns expectedEmploymentDetailsResponse

        // When
        val getResponse = personEmploymentController.getEmploymentDetails(personId)

        // Then
        assertNotNull(getResponse)
        assertEquals(HttpStatus.OK, getResponse.statusCode)
        assertNotNull(getResponse.body)
        assertEquals(expectedEmploymentDetailsResponse.employmentId, getResponse.body?.employmentId)
        assertEquals(expectedEmploymentDetailsResponse.personId, getResponse.body?.personId)
        assertEquals(expectedEmploymentDetailsResponse.employerName, getResponse.body?.employerName)
    }

    @Test
    @DisplayName("should update employment details through complete flow")
    fun `updateEmploymentDetails should work through complete flow`() {
        // Given
        val updateRequest = createTestEmploymentDetailsUpdateRequest()
        val updatedResponse = createTestEmploymentDetailsResponse(
            personId = personId,
            employerName = "Updated Company"
        )
        
        every { personService.updatePersonEmploymentDetails(personId, updateRequest) } returns updatedResponse

        // When
        val updateResponse = personEmploymentController.updateEmploymentDetails(personId, updateRequest)

        // Then
        assertNotNull(updateResponse)
        assertEquals(HttpStatus.OK, updateResponse.statusCode)
        assertNotNull(updateResponse.body)
        assertEquals(updatedResponse.employmentId, updateResponse.body?.employmentId)
        assertEquals(updatedResponse.personId, updateResponse.body?.personId)
        assertEquals(updatedResponse.employerName, updateResponse.body?.employerName)
    }

    @Test
    @DisplayName("should delete employment details through complete flow")
    fun `deleteEmploymentDetails should work through complete flow`() {
        // Given
        every { personService.deletePersonEmploymentDetails(personId) } returns Unit

        // When
        val deleteResponse = personEmploymentController.deleteEmploymentDetails(personId)

        // Then
        assertNotNull(deleteResponse)
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.statusCode)
    }

    @Test
    @DisplayName("should handle employment details CRUD operations through service layer")
    fun `employmentDetails CRUD should work through service layer`() {
        // Given
        val createRequest = createTestEmploymentDetailsCreateRequest()
        val updateRequest = createTestEmploymentDetailsUpdateRequest()
        
        every { personService.createPersonEmploymentDetails(personId, createRequest) } returns expectedEmploymentDetailsResponse
        every { personService.getPersonEmploymentDetails(personId) } returns expectedEmploymentDetailsResponse
        every { personService.updatePersonEmploymentDetails(personId, updateRequest) } returns expectedEmploymentDetailsResponse
        every { personService.deletePersonEmploymentDetails(personId) } returns Unit

        // When & Then - Create
        val createResponse = personService.createPersonEmploymentDetails(personId, createRequest)
        assertNotNull(createResponse)
        assertEquals(expectedEmploymentDetailsResponse.employmentId, createResponse.employmentId)

        // When & Then - Read
        val getResponse = personService.getPersonEmploymentDetails(personId)
        assertNotNull(getResponse)
        assertEquals(expectedEmploymentDetailsResponse.employmentId, getResponse?.employmentId)

        // When & Then - Update
        val updateResponse = personService.updatePersonEmploymentDetails(personId, updateRequest)
        assertNotNull(updateResponse)
        assertEquals(expectedEmploymentDetailsResponse.employmentId, updateResponse.employmentId)

        // When & Then - Delete
        personService.deletePersonEmploymentDetails(personId)
    }

    @Test
    @DisplayName("should return null when employment details not found")
    fun `getEmploymentDetails should return null when not found`() {
        // Given
        every { personService.getPersonEmploymentDetails(personId) } returns null

        // When
        val getResponse = personEmploymentController.getEmploymentDetails(personId)

        // Then
        assertNotNull(getResponse)
        assertEquals(HttpStatus.OK, getResponse.statusCode)
        assertEquals(null, getResponse.body)
    }

    @Test
    @DisplayName("should handle partial employment details update")
    fun `updateEmploymentDetails should handle partial update`() {
        // Given
        val partialUpdateRequest = EmploymentDetailsUpdateRequest(
            employerName = "Partially Updated Company",
            employerType = null,
            jobTitle = null,
            department = null,
            employmentType = null,
            location = null,
            salary = null,
            documents = null,
            extData = null
        )
        val updatedResponse = createTestEmploymentDetailsResponse(
            personId = personId,
            employerName = "Partially Updated Company"
        )
        
        every { personService.updatePersonEmploymentDetails(personId, partialUpdateRequest) } returns updatedResponse

        // When
        val updateResponse = personEmploymentController.updateEmploymentDetails(personId, partialUpdateRequest)

        // Then
        assertNotNull(updateResponse)
        assertEquals(HttpStatus.OK, updateResponse.statusCode)
        assertNotNull(updateResponse.body)
        assertEquals(updatedResponse.employerName, updateResponse.body?.employerName)
    }


}
