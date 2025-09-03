package com.nivasafinance.features.document.enum

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ProviderType Enum Tests")
class ProviderTypeTest {

    @Nested
    @DisplayName("ProviderType Values Tests")
    inner class ProviderTypeValuesTests {

        @Test
        @DisplayName("Should have correct enum values")
        fun `should have correct enum values`() {
            val values = ProviderType.values()

            assertEquals(2, values.size)
            assertEquals(ProviderType.AWS_S3, values[0])
            assertEquals(ProviderType.LOCAL, values[1])
        }

        @Test
        @DisplayName("Should have AWS_S3 provider type")
        fun `should have AWS_S3 provider type`() {
            val awsS3 = ProviderType.AWS_S3

            assertNotNull(awsS3)
            assertEquals("AWS_S3", awsS3.name)
        }

        @Test
        @DisplayName("Should have LOCAL provider type")
        fun `should have LOCAL provider type`() {
            val local = ProviderType.LOCAL

            assertNotNull(local)
            assertEquals("LOCAL", local.name)
        }
    }

    @Nested
    @DisplayName("ProviderType ValueOf Tests")
    inner class ProviderTypeValueOfTests {

        @Test
        @DisplayName("Should get AWS_S3 from string")
        fun `should get AWS_S3 from string`() {
            val provider = ProviderType.valueOf("AWS_S3")

            assertEquals(ProviderType.AWS_S3, provider)
        }

        @Test
        @DisplayName("Should get LOCAL from string")
        fun `should get LOCAL from string`() {
            val provider = ProviderType.valueOf("LOCAL")

            assertEquals(ProviderType.LOCAL, provider)
        }
    }

    @Nested
    @DisplayName("ProviderType Ordinal Tests")
    inner class ProviderTypeOrdinalTests {

        @Test
        @DisplayName("Should have correct ordinal values")
        fun `should have correct ordinal values`() {
            assertEquals(0, ProviderType.AWS_S3.ordinal)
            assertEquals(1, ProviderType.LOCAL.ordinal)
        }
    }
}
