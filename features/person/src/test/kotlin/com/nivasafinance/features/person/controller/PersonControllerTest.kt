package com.nivasafinance.features.person.controller

import com.nivasafinance.TestUtils.createTestPersonDto
import com.nivasafinance.features.person.service.PersonReadService
import com.nivasafinance.features.person.service.PersonWriteService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PersonController Tests")
class PersonControllerTest {

    private val personReadService = mockk<PersonReadService>()
    private val personWriteService = mockk<PersonWriteService>()
    private lateinit var personController: PersonController

    private val personId = UUID.randomUUID()
    private val expectedDto = createTestPersonDto(id = personId)

    @BeforeEach
    fun setup() {
        personController = PersonController(personReadService, personWriteService)
    }

    @Test
    @DisplayName("should return person when found")
    fun `getPerson should return person when found`() {
        // Given
        every { personReadService.getPerson(personId) } returns expectedDto

        // When
        val result = personController.getPerson(personId)

        // Then
        assertNotNull(result)
        assertEquals(expectedDto.id, result.body?.id)
        assertEquals(expectedDto.firstName, result.body?.firstName)
        assertEquals(expectedDto.lastName, result.body?.lastName)

        verify(exactly = 1) { personReadService.getPerson(personId) }
    }

    @Test
    @DisplayName("should create person successfully")
    fun `createPerson should create person successfully`() {
        // Given
        val newPersonDto = createTestPersonDto(id = null)
        every { personWriteService.savePerson(newPersonDto) } returns expectedDto

        // When
        val result = personController.createPerson(newPersonDto)

        // Then
        assertNotNull(result)
        assertEquals(expectedDto.id, result.body?.id)
        assertEquals(expectedDto.firstName, result.body?.firstName)
        assertEquals(expectedDto.lastName, result.body?.lastName)

        verify(exactly = 1) { personWriteService.savePerson(newPersonDto) }
    }

    @Test
    @DisplayName("should update person successfully")
    fun `updatePerson should update person successfully`() {
        // Given
        val updateDto = createTestPersonDto(id = personId)
        every { personWriteService.updatePerson(personId, updateDto) } returns expectedDto

        // When
        val result = personController.updatePerson(personId, updateDto)

        // Then
        assertNotNull(result)
        assertEquals(expectedDto.id, result.body?.id)
        assertEquals(expectedDto.firstName, result.body?.firstName)
        assertEquals(expectedDto.lastName, result.body?.lastName)

        verify(exactly = 1) { personWriteService.updatePerson(personId, updateDto) }
    }

    @Test
    @DisplayName("should delete person successfully")
    fun `deletePerson should delete person successfully`() {
        // Given
        every { personWriteService.deletePerson(personId) } returns Unit

        // When
        val result = personController.deletePerson(personId)

        // Then
        assertNotNull(result)
        assertEquals(204, result.statusCode.value())
        verify(exactly = 1) { personWriteService.deletePerson(personId) }
    }
}
