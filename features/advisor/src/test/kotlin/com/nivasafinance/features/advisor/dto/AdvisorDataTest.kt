package com.nivasafinance.features.advisor.dto

import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorData Tests")
class AdvisorDataTest {

    @Test
    @DisplayName("should create AdvisorData from entity")
    fun `fromEntity should create AdvisorData from entity`() {
        val id = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val advisor = Advisor(
            id = id,
            personId = personId,
            advisorCode = "ADV001",
            isEmployee = false,
            status = AdvisorStatus.CREATED,
            isExperiencedDsa = true,
            remarks = "Test advisor",
            rejectionReason = null,
            advisorFeedback = "Good advisor",
            welcomeKitSent = true,
            attendedAdvisorMeeting = false,
            extData = mapOf("key" to "value")
        )

        val advisorData = AdvisorData.fromEntity(advisor)

        assertNotNull(advisorData)
        assertEquals(id, advisorData.id)
        assertEquals(personId, advisorData.personId)
        assertEquals("ADV001", advisorData.advisorCode)
        assertEquals(false, advisorData.isEmployee)
        assertEquals(AdvisorStatus.CREATED, advisorData.status)
        assertEquals(true, advisorData.isExperiencedDsa)
        assertEquals("Test advisor", advisorData.remarks)
        assertEquals(null, advisorData.rejectionReason)
        assertEquals("Good advisor", advisorData.advisorFeedback)
        assertEquals(true, advisorData.welcomeKitSent)
        assertEquals(false, advisorData.attendedAdvisorMeeting)
        assertEquals(mapOf("key" to "value"), advisorData.extData)
    }

    @Test
    @DisplayName("should handle null values in entity")
    fun `fromEntity should handle null values in entity`() {
        val id = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val advisor = Advisor(
            id = id,
            personId = personId,
            advisorCode = "ADV002",
            isEmployee = true,
            status = AdvisorStatus.ACTIVE,
            isExperiencedDsa = false,
            remarks = null,
            rejectionReason = null,
            advisorFeedback = null,
            welcomeKitSent = false,
            attendedAdvisorMeeting = true,
            extData = null
        )

        val advisorData = AdvisorData.fromEntity(advisor)

        assertNotNull(advisorData)
        assertEquals(id, advisorData.id)
        assertEquals(personId, advisorData.personId)
        assertEquals("ADV002", advisorData.advisorCode)
        assertEquals(true, advisorData.isEmployee)
        assertEquals(AdvisorStatus.ACTIVE, advisorData.status)
        assertEquals(false, advisorData.isExperiencedDsa)
        assertEquals(null, advisorData.remarks)
        assertEquals(null, advisorData.rejectionReason)
        assertEquals(null, advisorData.advisorFeedback)
        assertEquals(false, advisorData.welcomeKitSent)
        assertEquals(true, advisorData.attendedAdvisorMeeting)
        assertEquals(null, advisorData.extData)
    }
}
