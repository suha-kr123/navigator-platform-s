package com.nivasafinance.features.otp.core.exception;

public final class OtpExceptionFactory {

    private OtpExceptionFactory() {
    }

    public static OtpValidationException invalidConfiguration(String message) {
        return new OtpValidationException(message);
    }

    public static OtpValidationException unsupportedGenerationMethod(String method) {
        return new OtpValidationException("Unsupported OTP generation method: " + method);
    }

    public static OtpValidationException tokenNotFound() {
        return new OtpValidationException("No OTP found for the provided token id");
    }

    public static OtpValidationException expiredOtp() {
        return new OtpValidationException("OTP has expired");
    }

    public static OtpValidationException invalidOtp() {
        return new OtpValidationException("Invalid OTP");
    }

    public static OtpValidationException missingTemplateName() {
        return new OtpValidationException("WhatsApp template name is required");
    }

    public static OtpValidationException missingRecipientPhone() {
        return new OtpValidationException("WhatsApp recipient phone is required");
    }

    public static OtpDeliveryFailedException deliveryFailed(String message) {
        return new OtpDeliveryFailedException(message);
    }

    public static OtpOperationException saveTokenFailed(Throwable cause) {
        return new OtpOperationException("Failed to save one time token", cause);
    }

    public static OtpOperationException retrieveTokenFailed(Throwable cause) {
        return new OtpOperationException("Failed to retrieve one time token", cause);
    }

    public static OtpOperationException retrieveConfigurationFailed(Throwable cause) {
        return new OtpOperationException("Failed to retrieve OTP configuration", cause);
    }
}
