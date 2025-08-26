package com.nivasafinance.features.lead.applicant.enum

import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("RelationshipToPrimary Tests")
class RelationshipToPrimaryTest {

    @Test
    @DisplayName("Should have correct enum values")
    fun `should have correct enum values`() {
        assertEquals(RelationshipToPrimary.SELF, RelationshipToPrimary.valueOf("SELF"))
        assertEquals(RelationshipToPrimary.SPOUSE, RelationshipToPrimary.valueOf("SPOUSE"))
        assertEquals(RelationshipToPrimary.FATHER, RelationshipToPrimary.valueOf("FATHER"))
        assertEquals(RelationshipToPrimary.MOTHER, RelationshipToPrimary.valueOf("MOTHER"))
        assertEquals(RelationshipToPrimary.BROTHER, RelationshipToPrimary.valueOf("BROTHER"))
        assertEquals(RelationshipToPrimary.SISTER, RelationshipToPrimary.valueOf("SISTER"))
        assertEquals(RelationshipToPrimary.FRIEND, RelationshipToPrimary.valueOf("FRIEND"))
        assertEquals(RelationshipToPrimary.BUSINESS_PARTNER, RelationshipToPrimary.valueOf("BUSINESS_PARTNER"))
    }

    @Test
    @DisplayName("Should have correct number of enum values")
    fun `should have correct number of enum values`() {
        assertEquals(8, RelationshipToPrimary.values().size)
    }
}
