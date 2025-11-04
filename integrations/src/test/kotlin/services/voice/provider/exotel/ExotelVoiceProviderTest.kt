package services.voice.provider.exotel

import framework.config.BusinessContext
import framework.config.ThirdPartyProviderList
import framework.core.data.ThirdPartyConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.voice.dto.VoiceCallRequest
import java.util.UUID

class ExotelVoiceProviderTest {

    private lateinit var provider: ExotelVoiceProvider
    private lateinit var config: ThirdPartyConfig
    private lateinit var businessContext: BusinessContext

    @BeforeEach
    fun setUp() {
        provider = ExotelVoiceProvider()
        config = ThirdPartyConfig(
            id = UUID.randomUUID(),
            name = "test_config_name",
            provider = ThirdPartyProviderList.EXOTEL.name,
            configurations = mapOf(
                "accountSid" to "test_account_sid",
                "authToken" to "test_auth_token",
                "subdomain" to "api.exotel.com",
                "callerId" to "09513886363",
                "webhookUrl" to "",
                "apiKey" to "",
                "apiToken" to "",
                "recordingEnabled" to "true",
                "maxCallDuration" to "3600",
                "retryAttempts" to "3",
                "timeout" to "30"
            )
        )
        businessContext = BusinessContext(
            entityName = "Lead",
            entityId = "123",
            businessPurpose = "Test call"
        )
    }

    @Test
    fun `getKey should return EXOTEL provider`() {
        // When
        val result = provider.getKey()

        // Then
        assertEquals(ThirdPartyProviderList.EXOTEL, result)
    }

    @Test
    fun `setupConfiguration should map configuration parameters correctly`() {
        // When
        val result = provider.setupConfiguration(config.configurations)

        // Then
        assertEquals("test_account_sid", result.accountSid)
        assertEquals("test_auth_token", result.authToken)
        assertEquals("api.exotel.com", result.subdomain)
        assertEquals("09513886363", result.callerId)
        assertEquals("", result.webhookUrl)
        assertEquals("", result.apiKey)
        assertEquals("", result.apiToken)
        assertTrue(result.recordingEnabled)
        assertEquals(3600, result.maxCallDuration)
        assertEquals(3, result.retryAttempts)
        assertEquals(30, result.timeout)
    }

    @Test
    fun `setupConfiguration should handle boolean conversion for recordingEnabled`() {
        // Given
        val configWithFalseRecording = config.copy(
            configurations = config.configurations + ("recordingEnabled" to "false")
        )

        // When
        val result = provider.setupConfiguration(configWithFalseRecording.configurations)

        // Then
        assertFalse(result.recordingEnabled)
    }

    @Test
    fun `setupConfiguration should handle numeric conversions`() {
        // Given
        val configWithCustomValues = config.copy(
            configurations = config.configurations + mapOf(
                "maxCallDuration" to "1800",
                "retryAttempts" to "5",
                "timeout" to "60"
            )
        )

        // When
        val result = provider.setupConfiguration(configWithCustomValues.configurations)

        // Then
        assertEquals(1800, result.maxCallDuration)
        assertEquals(5, result.retryAttempts)
        assertEquals(60, result.timeout)
    }

    @Test
    fun `makeCall should validate phone numbers and throw exception for empty fromNumber`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "",
            toNumber = "8296687083",
            entityName = "Lead",
            entityId = 123L
        )

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            provider.makeCall(request, config, businessContext)
        }
        assertEquals("From number cannot be empty", exception.message)
    }

    @Test
    fun `makeCall should validate phone numbers and throw exception for empty toNumber`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "7899515638",
            toNumber = "",
            entityName = "Lead",
            entityId = 123L
        )

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            provider.makeCall(request, config, businessContext)
        }
        assertEquals("To number cannot be empty", exception.message)
    }

    @Test
    fun `makeCall should validate phone numbers and throw exception for blank fromNumber`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "   ",
            toNumber = "8296687083",
            entityName = "Lead",
            entityId = 123L
        )

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            provider.makeCall(request, config, businessContext)
        }
        assertEquals("From number cannot be empty", exception.message)
    }

    @Test
    fun `makeCall should validate phone numbers and throw exception for blank toNumber`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "7899515638",
            toNumber = "   ",
            entityName = "Lead",
            entityId = 123L
        )

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            provider.makeCall(request, config, businessContext)
        }
        assertEquals("To number cannot be empty", exception.message)
    }

    @Test
    fun `setupConfiguration should use default values for missing parameters`() {
        // Given
        val minimalConfig = mapOf(
            "accountSid" to "test_account_sid",
            "authToken" to "test_auth_token"
        )

        // When
        val result = provider.setupConfiguration(minimalConfig)

        // Then
        assertEquals("test_account_sid", result.accountSid)
        assertEquals("test_auth_token", result.authToken)
        assertEquals("api.exotel.com", result.subdomain) // default
        assertEquals("", result.callerId) // default
        assertEquals("", result.webhookUrl) // default
        assertEquals("", result.apiKey) // default
        assertEquals("", result.apiToken) // default
        assertTrue(result.recordingEnabled) // default
        assertEquals(3600, result.maxCallDuration) // default
        assertEquals(3, result.retryAttempts) // default
        assertEquals(30, result.timeout) // default
    }

    @Test
    fun `setupConfiguration should handle empty string values`() {
        // Given
        val configWithEmptyStrings = mapOf(
            "accountSid" to "test_account_sid",
            "authToken" to "test_auth_token",
            "subdomain" to "",
            "callerId" to "",
            "webhookUrl" to "",
            "apiKey" to "",
            "apiToken" to "",
            "recordingEnabled" to "",
            "maxCallDuration" to "",
            "retryAttempts" to "",
            "timeout" to ""
        )

        // When
        val result = provider.setupConfiguration(configWithEmptyStrings)

        // Then
        assertEquals("test_account_sid", result.accountSid)
        assertEquals("test_auth_token", result.authToken)
        assertEquals("api.exotel.com", result.subdomain) // default when empty
        assertEquals("", result.callerId)
        assertEquals("", result.webhookUrl)
        assertEquals("", result.apiKey)
        assertEquals("", result.apiToken)
        assertTrue(result.recordingEnabled) // default when empty
        assertEquals(3600, result.maxCallDuration) // default when empty
        assertEquals(3, result.retryAttempts) // default when empty
        assertEquals(30, result.timeout) // default when empty
    }

    @Test
    fun `setupConfiguration should handle invalid boolean values`() {
        // Given
        val configWithInvalidBoolean = mapOf(
            "accountSid" to "test_account_sid",
            "authToken" to "test_auth_token",
            "recordingEnabled" to "invalid_boolean"
        )

        // When & Then
        // This should not throw an exception, but use default value
        val result = provider.setupConfiguration(configWithInvalidBoolean)
        assertTrue(result.recordingEnabled) // default value
    }

    @Test
    fun `setupConfiguration should handle invalid numeric values`() {
        // Given
        val configWithInvalidNumbers = mapOf(
            "accountSid" to "test_account_sid",
            "authToken" to "test_auth_token",
            "maxCallDuration" to "invalid_number",
            "retryAttempts" to "not_a_number",
            "timeout" to "also_invalid"
        )

        // When & Then
        // This should not throw an exception, but use default values
        val result = provider.setupConfiguration(configWithInvalidNumbers)
        assertEquals(3600, result.maxCallDuration) // default value
        assertEquals(3, result.retryAttempts) // default value
        assertEquals(30, result.timeout) // default value
    }

    @Test
    fun `setupConfiguration should handle null values gracefully`() {
        // Given
        val configWithNulls = mapOf(
            "accountSid" to "test_sid",
            "authToken" to "test_token",
            "callerId" to "test_caller"
        )

        // When
        val exotelConfig = provider.setupConfiguration(configWithNulls)

        // Then
        assertEquals("test_sid", exotelConfig.accountSid)
        assertEquals("test_token", exotelConfig.authToken)
        assertEquals("test_caller", exotelConfig.callerId)
        assertEquals("api.exotel.com", exotelConfig.subdomain) // Default
        assertEquals("", exotelConfig.apiKey) // Default
        assertEquals("", exotelConfig.apiToken) // Default
        assertTrue(exotelConfig.recordingEnabled) // Default
        assertEquals(3600, exotelConfig.maxCallDuration) // Default
        assertEquals(3, exotelConfig.retryAttempts) // Default
        assertEquals(30, exotelConfig.timeout) // Default
    }

    @Test
    fun `setupConfiguration should handle mixed valid and invalid values`() {
        // Given
        val mixedConfig = mapOf(
            "accountSid" to "valid_sid",
            "authToken" to "valid_token",
            "callerId" to "valid_caller",
            "subdomain" to "custom.exotel.com",
            "apiKey" to "valid_key",
            "apiToken" to "valid_token",
            "recordingEnabled" to "false", // Valid boolean
            "maxCallDuration" to "invalid_number", // Invalid number
            "retryAttempts" to "5", // Valid number
            "timeout" to "abc" // Invalid number
        )

        // When
        val exotelConfig = provider.setupConfiguration(mixedConfig)

        // Then
        assertEquals("valid_sid", exotelConfig.accountSid)
        assertEquals("valid_token", exotelConfig.authToken)
        assertEquals("valid_caller", exotelConfig.callerId)
        assertEquals("custom.exotel.com", exotelConfig.subdomain)
        assertEquals("valid_key", exotelConfig.apiKey)
        assertEquals("valid_token", exotelConfig.apiToken)
        assertFalse(exotelConfig.recordingEnabled) // Should parse "false" correctly
        assertEquals(3600, exotelConfig.maxCallDuration) // Should fallback to default
        assertEquals(5, exotelConfig.retryAttempts) // Should parse "5" correctly
        assertEquals(30, exotelConfig.timeout) // Should fallback to default
    }
}
