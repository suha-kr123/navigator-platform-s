package com.nivasafinance.features.advisor.dto

import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.person.dto.PersonResponse
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorResponse Tests")
class AdvisorResponseTest {

    @Test
    @DisplayName("should create AdvisorResponse with all fields")
    fun `AdvisorResponse should create with all fields`() {
        val id = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val personResponse = mockk<PersonResponse>()

        val response = AdvisorResponse(
            id = id,
            personId = personId,
            advisorCode = "ADV001",
            isEmployee = false,
            status = AdvisorStatus.CREATED,
            remarks = "Test advisor",
            rejectionReason = null,
            advisorFeedback = "Good advisor",
            welcomeKitSent = true,
            attendedAdvisorMeeting = false,
            extData = mapOf("key" to "value"),
            personalDetails = personResponse
        )

        assertNotNull(response)
        assertEquals(id, response.id)
        assertEquals(personId, response.personId)
        assertEquals("ADV001", response.advisorCode)
        assertEquals(false, response.isEmployee)
        assertEquals(AdvisorStatus.CREATED, response.status)
        assertEquals("Test advisor", response.remarks)
        assertEquals(null, response.rejectionReason)
        assertEquals("Good advisor", response.advisorFeedback)
        assertEquals(true, response.welcomeKitSent)
        assertEquals(false, response.attendedAdvisorMeeting)
        assertEquals(mapOf("key" to "value"), response.extData)
        assertEquals(personResponse, response.personalDetails)
    }

    @Test
    @DisplayName("should create AdvisorResponse with minimal fields")
    fun `AdvisorResponse should create with minimal fields`() {
        val id = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val personResponse = mockk<PersonResponse>()

        val response = AdvisorResponse(
            id = id,
            personId = personId,
            advisorCode = "ADV002",
            isEmployee = true,
            status = AdvisorStatus.ACTIVE,
            remarks = null,
            rejectionReason = null,
            advisorFeedback = null,
            welcomeKitSent = false,
            attendedAdvisorMeeting = true,
            extData = null,
            personalDetails = personResponse
        )

        assertNotNull(response)
        assertEquals(id, response.id)
        assertEquals(personId, response.personId)
        assertEquals("ADV002", response.advisorCode)
        assertEquals(true, response.isEmployee)
        assertEquals(AdvisorStatus.ACTIVE, response.status)
        assertEquals(null, response.remarks)
        assertEquals(null, response.rejectionReason)
        assertEquals(null, response.advisorFeedback)
        assertEquals(false, response.welcomeKitSent)
        assertEquals(true, response.attendedAdvisorMeeting)
        assertEquals(null, response.extData)
        assertEquals(personResponse, response.personalDetails)
    }
}
