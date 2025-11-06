package services.voice.provider.exotel;

import framework.core.exception.HttpClientException;
import framework.core.exception.PhoneNumberValidationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import services.voice.dto.VoiceCallRequest;
import services.voice.dto.VoiceCallResponse;
import services.voice.provider.exotel.data.ExotelConfiguration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExotelHttpClient {

    private static final int DEFAULT_MAX_CALL_DURATION = 3600;
    private static final int DEFAULT_TIMEOUT = 30;
    private static final int INDIAN_MOBILE_LENGTH = 10;
    private static final int INDIAN_COUNTRY_CODE_LENGTH = 12;
    private static final int INDIAN_COUNTRY_CODE_WITH_PLUS_LENGTH = 13;
    private static final String INDIAN_COUNTRY_CODE = "91";
    private static final int MIN_PHONE_LENGTH = 10;
    private static final int MAX_PHONE_LENGTH = 15;

    private final RestTemplate restTemplate;

    public ExotelHttpClient() {
        this.restTemplate = new RestTemplate();
    }

    public ExotelHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public VoiceCallResponse makeCall(ExotelConfiguration config, VoiceCallRequest request) {
        String url = "https://" + config.getSubdomain() + "/v1/Accounts/" + config.getAccountSid() + "/Calls/connect";

        // Normalize phone numbers for Exotel
        String normalizedFrom = normalizePhoneNumber(request.getFromNumber());
        String normalizedTo = normalizePhoneNumber(request.getToNumber());
        String normalizedCallerId = normalizeCallerId(config.getCallerId());

        // Validate normalized numbers
        if (!isValidPhoneNumber(normalizedFrom)) {
            throw new PhoneNumberValidationException("Invalid from number format: " + request.getFromNumber() + " -> " + normalizedFrom);
        }
        if (!isValidPhoneNumber(normalizedTo)) {
            throw new PhoneNumberValidationException("Invalid to number format: " + request.getToNumber() + " -> " + normalizedTo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Try API Key and API Token first, then fall back to accountSid and authToken
        String authString = (!config.getApiKey().isEmpty() && !config.getApiToken().isEmpty())
                ? getBasicAuth(config.getApiKey(), config.getApiToken())
                : getBasicAuth(config.getAccountSid(), config.getAuthToken());
        headers.set("Authorization", "Basic " + authString);

        StringBuilder body = new StringBuilder();
        body.append("From=").append(encodePhoneNumber(normalizedFrom)).append("&");
        body.append("To=").append(encodePhoneNumber(normalizedTo)).append("&");
        body.append("CallerId=").append(encodePhoneNumber(normalizedCallerId));
        // Add recording parameter
        if (config.isRecordingEnabled()) {
            body.append("&Record=true");
        } else {
            body.append("&Record=false");
        }
        if (config.getMaxCallDuration() != DEFAULT_MAX_CALL_DURATION) {
            body.append("&TimeLimit=").append(config.getMaxCallDuration());
        }
        if (config.getTimeout() != DEFAULT_TIMEOUT) {
            body.append("&TimeOut=").append(config.getTimeout());
        }

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse XML response
            String responseBody = response.getBody() != null ? response.getBody() : "";
            String callSid = extractXmlValue(responseBody, "CallSid");
            String status = extractXmlValue(responseBody, "Status") != null ? extractXmlValue(responseBody, "Status") : "";
            String errorMessage = extractXmlValue(responseBody, "ErrorMessage");

            // Try alternative XML parsing for CallSid
            String alternativeCallSid = extractXmlValue(responseBody, "Sid");

            String finalCallSid = callSid != null ? callSid : (alternativeCallSid != null ? alternativeCallSid : "");

            return new VoiceCallResponse(
                    finalCallSid,
                    status,
                    normalizedFrom,
                    normalizedTo,
                    null,
                    null,
                    errorMessage
            );
        } catch (HttpClientException e) {
            throw e;
        } catch (Exception e) {
            return new VoiceCallResponse(
                    "",
                    "failed",
                    "",
                    "",
                    null,
                    null,
                    e.getMessage() != null ? e.getMessage() : "Unknown error occurred"
            );
        }
    }

    public VoiceCallResponse getCallStatus(ExotelConfiguration config, String callSid) {
        String url = "https://" + config.getSubdomain() + "/v1/Accounts/" + config.getAccountSid() + "/Calls/" + callSid + ".json";

        HttpHeaders headers = new HttpHeaders();
        // Try API Key and API Token first, then fall back to accountSid and authToken
        String authString = (!config.getApiKey().isEmpty() && !config.getApiToken().isEmpty())
                ? getBasicAuth(config.getApiKey(), config.getApiToken())
                : getBasicAuth(config.getAccountSid(), config.getAuthToken());
        headers.set("Authorization", "Basic " + authString);

        HttpEntity<Object> entity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // Parse JSON response
            String responseBody = response.getBody() != null ? response.getBody() : "";
            String status = extractJsonValue(responseBody, "Status");
            if (status == null) {
                status = "unknown";
            }
            String fromNumber = extractJsonValue(responseBody, "From");
            if (fromNumber == null) {
                fromNumber = "";
            }
            String toNumber = extractJsonValue(responseBody, "To");
            if (toNumber == null) {
                toNumber = "";
            }
            Integer duration = null;
            String durationStr = extractJsonValue(responseBody, "Duration");
            if (durationStr != null) {
                try {
                    duration = Integer.parseInt(durationStr);
                } catch (NumberFormatException ignored) {
                    // Ignore parsing errors
                }
            }
            String recordingUrl = extractJsonValue(responseBody, "RecordingUrl");
            String errorMessage = extractJsonValue(responseBody, "ErrorMessage");

            return new VoiceCallResponse(
                    callSid,
                    status,
                    fromNumber,
                    toNumber,
                    duration,
                    recordingUrl,
                    errorMessage
            );
        } catch (HttpClientException e) {
            throw e;
        } catch (Exception e) {
            return new VoiceCallResponse(
                    callSid,
                    "failed",
                    "",
                    "",
                    null,
                    null,
                    e.getMessage() != null ? e.getMessage() : "Unknown error occurred"
            );
        }
    }

    private String getBasicAuth(String accountSid, String authToken) {
        // Use accountSid and authToken for Exotel API authentication
        String credentials = accountSid + ":" + authToken;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private String encodePhoneNumber(String phoneNumber) {
        // Remove any non-digit characters except + and format for Exotel
        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");
        return URLEncoder.encode(cleaned, StandardCharsets.UTF_8);
    }

    /**
     * Normalizes CallerId for Exotel API calls.
     * Removes dashes and other formatting characters.
     */
    private String normalizeCallerId(String callerId) {
        // Remove all non-digit characters except +
        return callerId.replaceAll("[^+0-9]", "");
    }

    private String extractXmlValue(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">");
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractJsonValue(String json, String key) {
        // Simple JSON parsing - look for the key directly in the entire JSON
        // Handle both string values (quoted) and numeric values (unquoted)
        Pattern stringPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Pattern numberPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9]+)");

        Matcher stringMatcher = stringPattern.matcher(json);
        Matcher numberMatcher = numberPattern.matcher(json);

        if (stringMatcher.find()) {
            return stringMatcher.group(1);
        }
        if (numberMatcher.find()) {
            return numberMatcher.group(1);
        }
        return null;
    }

    /**
     * Normalizes phone numbers for Exotel API calls.
     * Handles Indian mobile numbers and international formats.
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        if (phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^+0-9]", "").trim();

        // Handle empty after cleaning
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Phone number contains no valid digits");
        }

        if (isIndianMobileNumber(cleaned)) {
            return normalizeIndianNumber(cleaned);
        } else if (cleaned.startsWith("+")) {
            return cleaned;
        } else if (cleaned.startsWith(INDIAN_COUNTRY_CODE) && cleaned.length() >= INDIAN_COUNTRY_CODE_LENGTH) {
            return "+" + cleaned;
        } else {
            return "+" + cleaned;
        }
    }

    /**
     * Checks if the cleaned number is an Indian mobile number
     */
    private boolean isIndianMobileNumber(String cleaned) {
        // Indian mobile: 10 digits starting with 6, 7, 8, or 9
        Pattern indianPattern = Pattern.compile("^[6-9]\\d{9}$");
        return indianPattern.matcher(cleaned).matches();
    }

    /**
     * Normalizes Indian mobile numbers
     */
    private String normalizeIndianNumber(String cleaned) {
        if (cleaned.length() == INDIAN_MOBILE_LENGTH && cleaned.charAt(0) >= '6' && cleaned.charAt(0) <= '9') {
            return "+" + INDIAN_COUNTRY_CODE + cleaned;
        } else if (cleaned.length() == INDIAN_COUNTRY_CODE_LENGTH && cleaned.startsWith(INDIAN_COUNTRY_CODE)) {
            return "+" + cleaned;
        } else if (cleaned.length() == INDIAN_COUNTRY_CODE_WITH_PLUS_LENGTH &&
                cleaned.startsWith("+" + INDIAN_COUNTRY_CODE)) {
            return cleaned;
        } else {
            return "+" + INDIAN_COUNTRY_CODE + cleaned;
        }
    }

    /**
     * Validates if the normalized number is in correct format for Exotel
     */
    private boolean isValidPhoneNumber(String normalizedNumber) {
        // Exotel expects numbers in international format starting with +
        if (!normalizedNumber.startsWith("+")) {
            return false;
        }
        if (normalizedNumber.length() < MIN_PHONE_LENGTH || normalizedNumber.length() > MAX_PHONE_LENGTH) {
            return false;
        }
        String digits = normalizedNumber.substring(1);
        return digits.chars().allMatch(Character::isDigit);
    }
}

