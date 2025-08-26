package com.nivasafinance.features.advisor.dto

import com.nivasafinance.features.advisor.enum.AdvisorStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorUpdateRequest Tests")
class AdvisorUpdateRequestTest {

    @Test
    @DisplayName("should create AdvisorUpdateRequest with all fields")
    fun `AdvisorUpdateRequest should create with all fields`() {
        val request = AdvisorUpdateRequest(
            advisorCode = "ADV001",
            isEmployee = false,
            status = AdvisorStatus.ACTIVE,
            remarks = "Updated advisor",
            rejectionReason = "Some reason",
            advisorFeedback = "Updated feedback",
            welcomeKitSent = true,
            attendedAdvisorMeeting = false,
            extData = mapOf("key" to "value")
        )

        assertNotNull(request)
        assertEquals("ADV001", request.advisorCode)
        assertEquals(false, request.isEmployee)
        assertEquals(AdvisorStatus.ACTIVE, request.status)
        assertEquals("Updated advisor", request.remarks)
        assertEquals("Some reason", request.rejectionReason)
        assertEquals("Updated feedback", request.advisorFeedback)
        assertEquals(true, request.welcomeKitSent)
        assertEquals(false, request.attendedAdvisorMeeting)
        assertEquals(mapOf("key" to "value"), request.extData)
    }

    @Test
    @DisplayName("should create AdvisorUpdateRequest with minimal fields")
    fun `AdvisorUpdateRequest should create with minimal fields`() {
        val request = AdvisorUpdateRequest(
            advisorCode = "ADV002"
        )

        assertNotNull(request)
        assertEquals("ADV002", request.advisorCode)
        assertEquals(null, request.isEmployee)
        assertEquals(null, request.status)
        assertEquals(null, request.remarks)
        assertEquals(null, request.rejectionReason)
        assertEquals(null, request.advisorFeedback)
        assertEquals(null, request.welcomeKitSent)
        assertEquals(null, request.attendedAdvisorMeeting)
        assertEquals(null, request.extData)
    }
}
