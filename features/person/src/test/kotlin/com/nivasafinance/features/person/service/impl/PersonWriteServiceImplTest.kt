package com.nivasafinance.features.person.service.impl

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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.*
import kotlin.test.assertEquals

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

    @Test
    fun `savePerson should save and return person`() {
        val personDto = PersonDto(firstName = "John", lastName = "Doe")
        val personEntity = Person(UUID.randomUUID(), "John", "Doe")
        val savedEntity = Person(id = personId, firstName = "John", lastName = "Doe")

        every { modelMapper.map(personDto, Person::class.java) } returns personEntity
        every { personRepository.save(personEntity) } returns savedEntity
        every {
            modelMapper.map(savedEntity, PersonDto::class.java)
        } returns PersonDto(id = personId, firstName = "John", lastName = "Doe")

        val result = personWriteService.savePerson(personDto)

        assertEquals(personId, result.id)
        assertEquals("John", result.firstName)

        verify(exactly = 1) { modelMapper.map(personDto, Person::class.java) }
        verify(exactly = 1) { personRepository.save(personEntity) }
        verify(exactly = 1) { modelMapper.map(savedEntity, PersonDto::class.java) }
    }

    @Test
    fun `deletePerson should delete when person exists`() {
        every { personRepository.existsById(personId) } returns true
        every { personRepository.deleteById(personId) } just Runs

        personWriteService.deletePerson(personId)

        verify(exactly = 1) { personRepository.existsById(personId) }
        verify(exactly = 1) { personRepository.deleteById(personId) }
    }

    @Test
    fun `deletePerson should throw exception when person not found`() {
        every { personRepository.existsById(personId) } returns false
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"

        assertThrows<PersonNotFoundException> {
            personWriteService.deletePerson(personId)
        }

        verify(exactly = 1) { personRepository.existsById(personId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }

    @Test
    fun `updatePerson should update and return person`() {
        val personDto = PersonDto(firstName = "Updated", lastName = "Name")
        val existingPerson = Person(id = personId, firstName = "Old", lastName = "Name")
        val updatedPerson = Person(id = personId, firstName = "Updated", lastName = "Name")

        every { personRepository.findById(personId) } returns Optional.of(existingPerson)
        every { modelMapper.map(personDto, existingPerson) } just Runs
        every { personRepository.save(existingPerson) } returns updatedPerson
        every {
            modelMapper.map(updatedPerson, PersonDto::class.java)
        } returns PersonDto(id = personId, firstName = "Updated", lastName = "Name")

        val result = personWriteService.updatePerson(personId, personDto)

        assertEquals(personId, result.id)
        assertEquals("Updated", result.firstName)

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { modelMapper.map(personDto, existingPerson) }
        verify(exactly = 1) { personRepository.save(existingPerson) }
        verify(exactly = 1) { modelMapper.map(updatedPerson, PersonDto::class.java) }
    }

    @Test
    fun `updatePerson should throw exception when person not found`() {
        val personDto = PersonDto(firstName = "Updated")

        every { personRepository.findById(personId) } returns Optional.empty()
        every { messageSource.getMessage(any(), any(), any()) } returns "Person not found"

        assertThrows<PersonNotFoundException> {
            personWriteService.updatePerson(personId, personDto)
        }

        verify(exactly = 1) { personRepository.findById(personId) }
        verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
    }
}
