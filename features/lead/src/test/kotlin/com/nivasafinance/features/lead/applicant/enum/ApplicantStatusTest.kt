package com.nivasafinance.features.lead.applicant.enum

import com.nivasafinance.features.applicant.enum.ApplicantStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ApplicantStatus Tests")
class ApplicantStatusTest {

    @Test
    @DisplayName("Should have correct enum values")
    fun `should have correct enum values`() {
        assertEquals(ApplicantStatus.NEEDS_TO_BE_REVIEWED, ApplicantStatus.valueOf("NEEDS_TO_BE_REVIEWED"))
        assertEquals(ApplicantStatus.UNDER_REVIEW, ApplicantStatus.valueOf("UNDER_REVIEW"))
        assertEquals(ApplicantStatus.REVIEWED, ApplicantStatus.valueOf("REVIEWED"))
        assertEquals(ApplicantStatus.INACTIVE, ApplicantStatus.valueOf("INACTIVE"))
    }

    @Test
    @DisplayName("Should have correct number of enum values")
    fun `should have correct number of enum values`() {
        assertEquals(4, ApplicantStatus.values().size)
    }
}
