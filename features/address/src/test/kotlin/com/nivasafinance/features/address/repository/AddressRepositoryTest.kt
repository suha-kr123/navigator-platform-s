package com.nivasafinance.features.address.repository

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("AddressRepository Tests")
class AddressRepositoryTest {

    @Test
    @DisplayName("repository interface should be properly defined")
    fun `repository interface should be properly defined`() {
        // This test verifies that the repository interface is properly structured
        // In a real scenario, this would be tested with @DataJpaTest
        assertNotNull(AddressRepository::class.java)
        assertEquals("AddressRepository", AddressRepository::class.simpleName)
    }

    @Test
    @DisplayName("repository should extend JpaRepository")
    fun `repository should extend JpaRepository`() {
        // This test verifies that the repository extends JpaRepository
        val interfaces = AddressRepository::class.java.interfaces
        assertNotNull(interfaces)
        assertTrue(interfaces.any { it.simpleName == "JpaRepository" })
    }
}
