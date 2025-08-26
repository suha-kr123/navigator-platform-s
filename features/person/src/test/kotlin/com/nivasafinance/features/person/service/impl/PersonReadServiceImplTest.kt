package com.nivasafinance.features.person.service.impl

import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonAddressMappingRepository
import com.nivasafinance.features.person.repository.PersonIdentifierRepository
import com.nivasafinance.features.person.repository.PersonRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PersonReadServiceImpl Tests")
class PersonReadServiceImplTest {

    private val personRepository = mockk<PersonRepository>()
    private val personAddressMappingRepository = mockk<PersonAddressMappingRepository>()
    private val personIdentifierRepository = mockk<PersonIdentifierRepository>()
    private val addressService = mockk<AddressService>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var personReadService: PersonReadServiceImpl

    private val personId = UUID.randomUUID()
    private val person = com.nivasafinance.features.person.entity.Person(
        id = personId,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        gender = data.enums.Gender.MALE
    )
    private val expectedData = PersonData.fromEntity(person)

    @BeforeEach
    fun setup() {
        personReadService = PersonReadServiceImpl(
            personRepository,
            personAddressMappingRepository,
            personIdentifierRepository,
            addressService
        )

        // Mock modelMapper for BaseNavigatorService using reflection
        val modelMapperField = personReadService.javaClass.superclass.getDeclaredField("modelMapper")
        modelMapperField.isAccessible = true
        modelMapperField.set(personReadService, modelMapper)

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = personReadService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(personReadService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"
    }

    @Test
    @DisplayName("should return person when found")
    fun `getPerson should return person when found`() {
        // Given
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When
        val result = personReadService.getPerson(personId)

        // Then
        assertNotNull(result)
        assertEquals(expectedData.id, result.id)
        assertEquals(expectedData.firstName, result.firstName)
        assertEquals(expectedData.lastName, result.lastName)

        verify(exactly = 1) { personRepository.findById(personId) }
    }

    @Test
    @DisplayName("should throw exception when not found")
    fun `getPerson should throw exception when not found`() {
        // Given
        every { personRepository.findById(personId) } returns Optional.empty()

        // When & Then
        assertThrows<PersonNotFoundException> {
            personReadService.getPerson(personId)
        }

        verify(exactly = 1) { personRepository.findById(personId) }
    }
}
