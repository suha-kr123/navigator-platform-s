package com.nivasafinance.features.advisor.dto

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorCreateRequest Tests")
class AdvisorCreateRequestTest {

    @Test
    @DisplayName("should create AdvisorCreateRequest with all fields")
    fun `AdvisorCreateRequest should create with all fields`() {
        val personId = UUID.randomUUID()
        val request = AdvisorCreateRequest(
            personId = personId,
            advisorCode = "ADV001",
            isEmployee = false,
            remarks = "Test advisor",
            rejectionReason = null,
            advisorFeedback = "Good advisor",
            welcomeKitSent = true,
            attendedAdvisorMeeting = false,
            extData = mapOf("key" to "value")
        )

        assertNotNull(request)
        assertEquals(personId, request.personId)
        assertEquals("ADV001", request.advisorCode)
        assertEquals(false, request.isEmployee)
        assertEquals("Test advisor", request.remarks)
        assertEquals(null, request.rejectionReason)
        assertEquals("Good advisor", request.advisorFeedback)
        assertEquals(true, request.welcomeKitSent)
        assertEquals(false, request.attendedAdvisorMeeting)
        assertEquals(mapOf("key" to "value"), request.extData)
    }

    @Test
    @DisplayName("should create AdvisorCreateRequest with minimal fields")
    fun `AdvisorCreateRequest should create with minimal fields`() {
        val personId = UUID.randomUUID()
        val request = AdvisorCreateRequest(
            personId = personId,
            advisorCode = "ADV002",
            isEmployee = true
        )

        assertNotNull(request)
        assertEquals(personId, request.personId)
        assertEquals("ADV002", request.advisorCode)
        assertEquals(true, request.isEmployee)
        assertEquals(null, request.remarks)
        assertEquals(null, request.rejectionReason)
        assertEquals(null, request.advisorFeedback)
        assertEquals(false, request.welcomeKitSent)
        assertEquals(false, request.attendedAdvisorMeeting)
        assertEquals(null, request.extData)
    }
}
