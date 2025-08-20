package com.nivasafinance.features.person.service.impl

import com.nivasafinance.TestUtils.createTestPerson
import com.nivasafinance.TestUtils.createTestPersonDto
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.exception.PersonNotFoundException
import com.nivasafinance.features.person.repository.PersonRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PersonWriteServiceImpl Tests")
class PersonWriteServiceImplTest {

    private val personRepository = mockk<PersonRepository>()
    private val messageSource = mockk<MessageSource>()
    private val modelMapper = mockk<ModelMapper>()

    private lateinit var personWriteService: PersonWriteServiceImpl

    private val personId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        personWriteService = PersonWriteServiceImpl(personRepository).apply {
            this.modelMapper = this@PersonWriteServiceImplTest.modelMapper
            this.messageSource = this@PersonWriteServiceImplTest.messageSource
        }
    }

    @Nested
    @DisplayName("savePerson Tests")
    inner class SavePersonTests {

        @Test
        @DisplayName("should save and return person")
        fun `savePerson should save and return person`() {
            // Given
            val personDto = createTestPersonDto(id = null)
            val personEntity = createTestPerson(id = UUID.randomUUID())
            val savedEntity = createTestPerson(id = personId)
            val expectedDto = createTestPersonDto(id = personId)

            every { modelMapper.map(personDto, Person::class.java) } returns personEntity
            every { personRepository.save(personEntity) } returns savedEntity
            every { modelMapper.map(savedEntity, PersonDto::class.java) } returns expectedDto

            // When
            val result = personWriteService.savePerson(personDto)

            // Then
            assertNotNull(result)
            assertEquals(personId, result.id)
            assertEquals(expectedDto.firstName, result.firstName)

            verify(exactly = 1) { modelMapper.map(personDto, Person::class.java) }
            verify(exactly = 1) { personRepository.save(personEntity) }
            verify(exactly = 1) { modelMapper.map(savedEntity, PersonDto::class.java) }
        }
    }

    @Nested
    @DisplayName("deletePerson Tests")
    inner class DeletePersonTests {

        @Test
        @DisplayName("should delete when person exists")
        fun `deletePerson should delete when person exists`() {
            // Given
            every { personRepository.existsById(personId) } returns true
            every { personRepository.deleteById(personId) } just Runs

            // When
            personWriteService.deletePerson(personId)

            // Then
            verify(exactly = 1) { personRepository.existsById(personId) }
            verify(exactly = 1) { personRepository.deleteById(personId) }
        }

        @Test
        @DisplayName("should throw exception when person not found")
        fun `deletePerson should throw exception when person not found`() {
            // Given
            every { personRepository.existsById(personId) } returns false
            every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"

            // When & Then
            assertThrows<PersonNotFoundException> {
                personWriteService.deletePerson(personId)
            }

            verify(exactly = 1) { personRepository.existsById(personId) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        }
    }

    @Nested
    @DisplayName("updatePerson Tests")
    inner class UpdatePersonTests {

        @Test
        @DisplayName("should update and return person")
        fun `updatePerson should update and return person`() {
            // Given
            val personDto = createTestPersonDto(
                id = null,
                firstName = "Updated",
                lastName = "Name"
            )
            val existingPerson = createTestPerson(
                id = personId,
                firstName = "Old",
                lastName = "Name"
            )
            val updatedPerson = createTestPerson(
                id = personId,
                firstName = "Updated",
                lastName = "Name"
            )
            val expectedDto = createTestPersonDto(
                id = personId,
                firstName = "Updated",
                lastName = "Name"
            )

            every { personRepository.findById(personId) } returns Optional.of(existingPerson)
            every { modelMapper.map(personDto, existingPerson) } just Runs
            every { personRepository.save(existingPerson) } returns updatedPerson
            every { modelMapper.map(updatedPerson, PersonDto::class.java) } returns expectedDto

            // When
            val result = personWriteService.updatePerson(personId, personDto)

            // Then
            assertNotNull(result)
            assertEquals(personId, result.id)
            assertEquals("Updated", result.firstName)

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 1) { modelMapper.map(personDto, existingPerson) }
            verify(exactly = 1) { personRepository.save(existingPerson) }
            verify(exactly = 1) { modelMapper.map(updatedPerson, PersonDto::class.java) }
        }

        @Test
        @DisplayName("should throw exception when person not found")
        fun `updatePerson should throw exception when person not found`() {
            // Given
            val personDto = createTestPersonDto(id = null, firstName = "Updated")

            every { personRepository.findById(personId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"

            // When & Then
            assertThrows<PersonNotFoundException> {
                personWriteService.updatePerson(personId, personDto)
            }

            verify(exactly = 1) { personRepository.findById(personId) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        }
    }
}
