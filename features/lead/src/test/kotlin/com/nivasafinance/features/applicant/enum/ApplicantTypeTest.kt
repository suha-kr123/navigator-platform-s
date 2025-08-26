package com.nivasafinance.features.applicant.enum

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ApplicantType Tests")
class ApplicantTypeTest {

    @Test
    @DisplayName("Should have correct enum values")
    fun `should have correct enum values`() {
        assertEquals(ApplicantType.PRIMARY, ApplicantType.valueOf("PRIMARY"))
        assertEquals(ApplicantType.CO_APPLICANT, ApplicantType.valueOf("CO_APPLICANT"))
        assertEquals(ApplicantType.GUARANTOR, ApplicantType.valueOf("GUARANTOR"))
    }

    @Test
    @DisplayName("Should have correct number of enum values")
    fun `should have correct number of enum values`() {
        assertEquals(3, ApplicantType.values().size)
    }
}
