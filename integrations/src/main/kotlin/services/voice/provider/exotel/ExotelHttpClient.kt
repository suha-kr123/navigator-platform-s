package services.voice.provider.exotel

import framework.core.exception.HttpClientException
import framework.core.exception.PhoneNumberValidationException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.provider.exotel.data.ExotelConfiguration
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class ExotelHttpClient(private val restTemplate: RestTemplate = RestTemplate()) {

    companion object {
        private const val DEFAULT_MAX_CALL_DURATION = 3600
        private const val DEFAULT_TIMEOUT = 30
        private const val INDIAN_MOBILE_LENGTH = 10
        private const val INDIAN_COUNTRY_CODE_LENGTH = 12
        private const val INDIAN_COUNTRY_CODE_WITH_PLUS_LENGTH = 13
        private const val INDIAN_COUNTRY_CODE = "91"
        private const val MIN_PHONE_LENGTH = 10
        private const val MAX_PHONE_LENGTH = 15
    }

    @Suppress("ThrowsCount", "TooGenericExceptionCaught", "SwallowedException")
    fun makeCall(config: ExotelConfiguration, request: VoiceCallRequest): VoiceCallResponse {
        val url = "https://${config.subdomain}/v1/Accounts/${config.accountSid}/Calls/connect"

        // Normalize phone numbers for Exotel
        val normalizedFrom = normalizePhoneNumber(request.fromNumber)
        val normalizedTo = normalizePhoneNumber(request.toNumber)
        val normalizedCallerId = normalizeCallerId(config.callerId)

        // Validate normalized numbers
        if (!isValidPhoneNumber(normalizedFrom)) {
            throw PhoneNumberValidationException("Invalid from number format: ${request.fromNumber} -> $normalizedFrom")
        }
        if (!isValidPhoneNumber(normalizedTo)) {
            throw PhoneNumberValidationException("Invalid to number format: ${request.toNumber} -> $normalizedTo")
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        // Try API Key and API Token first, then fall back to accountSid and authToken
        val authString = if (config.apiKey.isNotEmpty() && config.apiToken.isNotEmpty()) {
            getBasicAuth(config.apiKey, config.apiToken)
        } else {
            getBasicAuth(config.accountSid, config.authToken)
        }
        headers.set("Authorization", "Basic $authString")

        val body = buildString {
            append("From=${encodePhoneNumber(normalizedFrom)}&")
            append("To=${encodePhoneNumber(normalizedTo)}&")
            append("CallerId=${encodePhoneNumber(normalizedCallerId)}")
            // Add recording parameter
            if (config.recordingEnabled) {
                append("&Record=true")
            } else {
                append("&Record=false")
            }
            if (config.maxCallDuration != DEFAULT_MAX_CALL_DURATION) {
                append("&TimeLimit=${config.maxCallDuration}")
            }
            if (config.timeout != DEFAULT_TIMEOUT) {
                append("&TimeOut=${config.timeout}")
            }
        }

        val entity = HttpEntity(body, headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)

            // Parse XML response
            val responseBody = response.body.orEmpty()
            val callSid = extractXmlValue(responseBody, "CallSid")
            val status = extractXmlValue(responseBody, "Status").orEmpty()
            val errorMessage = extractXmlValue(responseBody, "ErrorMessage")

            // Try alternative XML parsing for CallSid
            val alternativeCallSid = extractXmlValue(responseBody, "Sid")

            val finalCallSid = callSid ?: alternativeCallSid.orEmpty()

            VoiceCallResponse(
                finalCallSid,
                status,
                normalizedFrom,
                normalizedTo,
                null,
                null,
                errorMessage
            )
        } catch (e: HttpClientException) {
            throw e
        } catch (e: Exception) {
            return VoiceCallResponse(
                callSid = "",
                status = "failed",
                fromNumber = "",
                toNumber = "",
                duration = null,
                recordingUrl = null,
                errorMessage = e.message ?: "Unknown error occurred"
            )
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun getCallStatus(config: ExotelConfiguration, callSid: String): VoiceCallResponse {
        val url = "https://${config.subdomain}/v1/Accounts/${config.accountSid}/Calls/$callSid.json"

        val headers = HttpHeaders()
        // Try API Key and API Token first, then fall back to accountSid and authToken
        val authString = if (config.apiKey.isNotEmpty() && config.apiToken.isNotEmpty()) {
            getBasicAuth(config.apiKey, config.apiToken)
        } else {
            getBasicAuth(config.accountSid, config.authToken)
        }
        headers.set("Authorization", "Basic $authString")

        val entity = HttpEntity<Any>(headers)

        return try {
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)

            // Parse JSON response
            val responseBody = response.body.orEmpty()
            val status = extractJsonValue(responseBody, "Status") ?: "unknown"
            val fromNumber = extractJsonValue(responseBody, "From").orEmpty()
            val toNumber = extractJsonValue(responseBody, "To").orEmpty()
            val duration = extractJsonValue(responseBody, "Duration")?.toIntOrNull()
            val recordingUrl = extractJsonValue(responseBody, "RecordingUrl")
            val errorMessage = extractJsonValue(responseBody, "ErrorMessage")

            VoiceCallResponse(
                callSid,
                status,
                fromNumber,
                toNumber,
                duration,
                recordingUrl,
                errorMessage
            )
        } catch (e: HttpClientException) {
            throw e
        } catch (e: Exception) {
            return VoiceCallResponse(
                callSid = callSid,
                status = "failed",
                fromNumber = "",
                toNumber = "",
                duration = null,
                recordingUrl = null,
                errorMessage = e.message ?: "Unknown error occurred"
            )
        }
    }

    private fun getBasicAuth(accountSid: String, authToken: String): String {
        // Use accountSid and authToken for Exotel API authentication
        val credentials = "$accountSid:$authToken"
        return Base64.getEncoder().encodeToString(credentials.toByteArray())
    }

    private fun encodePhoneNumber(phoneNumber: String): String {
        // Remove any non-digit characters except + and format for Exotel
        val cleaned = phoneNumber.replace(Regex("[^+0-9]"), "")
        return URLEncoder.encode(cleaned, StandardCharsets.UTF_8.toString())
    }

    /**
     * Normalizes CallerId for Exotel API calls.
     * Removes dashes and other formatting characters.
     */
    private fun normalizeCallerId(callerId: String): String {
        // Remove all non-digit characters except +
        return callerId.replace(Regex("[^+0-9]"), "")
    }

    private fun extractXmlValue(xml: String, tagName: String): String? {
        val regex = "<$tagName>(.*?)</$tagName>".toRegex()
        val matchResult = regex.find(xml)
        return matchResult?.groupValues?.get(1)
    }

    private fun extractJsonValue(json: String, key: String): String? {
        // Simple JSON parsing - look for the key directly in the entire JSON
        // Handle both string values (quoted) and numeric values (unquoted)
        val stringRegex = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        val numberRegex = "\"$key\"\\s*:\\s*([0-9]+)".toRegex()

        val stringMatch = stringRegex.find(json)
        val numberMatch = numberRegex.find(json)

        return stringMatch?.groupValues?.get(1) ?: numberMatch?.groupValues?.get(1)
    }

    /**
     * Normalizes phone numbers for Exotel API calls.
     * Handles Indian mobile numbers and international formats.
     */
    private fun normalizePhoneNumber(phoneNumber: String?): String {
        requireNotNull(phoneNumber) { "Phone number cannot be null" }
        require(phoneNumber.isNotBlank()) { "Phone number cannot be empty" }

        // Remove all non-digit characters except +
        val cleaned = phoneNumber.replace(Regex("[^+0-9]"), "").trim()

        // Handle empty after cleaning
        require(cleaned.isNotEmpty()) { "Phone number contains no valid digits" }

        return when {
            isIndianMobileNumber(cleaned) -> normalizeIndianNumber(cleaned)
            cleaned.startsWith("+") -> cleaned
            cleaned.startsWith(INDIAN_COUNTRY_CODE) && cleaned.length >= INDIAN_COUNTRY_CODE_LENGTH -> "+$cleaned"
            else -> "+$cleaned"
        }
    }

    /**
     * Checks if the cleaned number is an Indian mobile number
     */
    private fun isIndianMobileNumber(cleaned: String): Boolean {
        // Indian mobile: 10 digits starting with 6, 7, 8, or 9
        val indianPattern = Regex("^[6-9]\\d{9}$")
        return indianPattern.matches(cleaned)
    }

    /**
     * Normalizes Indian mobile numbers
     */
    private fun normalizeIndianNumber(cleaned: String): String {
        return when {
            cleaned.length == INDIAN_MOBILE_LENGTH && cleaned[0] in '6'..'9' ->
                "+$INDIAN_COUNTRY_CODE$cleaned"
            cleaned.length == INDIAN_COUNTRY_CODE_LENGTH && cleaned.startsWith(INDIAN_COUNTRY_CODE) -> "+$cleaned"
            cleaned.length == INDIAN_COUNTRY_CODE_WITH_PLUS_LENGTH &&
                cleaned.startsWith("+$INDIAN_COUNTRY_CODE") -> cleaned
            else -> "+$INDIAN_COUNTRY_CODE$cleaned"
        }
    }

    /**
     * Validates if the normalized number is in correct format for Exotel
     */
    private fun isValidPhoneNumber(normalizedNumber: String): Boolean {
        // Exotel expects numbers in international format starting with +
        return normalizedNumber.startsWith("+") &&
            normalizedNumber.length >= MIN_PHONE_LENGTH &&
            normalizedNumber.length <= MAX_PHONE_LENGTH &&
            normalizedNumber.substring(1).all { it.isDigit() }
    }
}
