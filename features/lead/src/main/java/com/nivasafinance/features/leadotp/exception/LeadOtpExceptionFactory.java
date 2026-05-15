package com.nivasafinance.features.leadotp.exception;

public final class LeadOtpExceptionFactory {

    private LeadOtpExceptionFactory() {
    }

    public static InvalidLeadOtpContactException invalidContactForLead() {
        return new InvalidLeadOtpContactException("Contact does not belong to the provided lead identifier");
    }

    public static LeadOtpMissingPrimaryMobileException missingPrimaryMobile() {
        return new LeadOtpMissingPrimaryMobileException("Current contact does not have a primary mobile number");
    }

    public static LeadOtpActiveTokenNotFoundException activeTokenNotFound() {
        return new LeadOtpActiveTokenNotFoundException("No active OTP found for the provided token");
    }

    public static InvalidLeadOtpContactException invalidCreateLeadMobile() {
        return new InvalidLeadOtpContactException("A valid mobile number is required");
    }

    public static InvalidLeadOtpContactException invalidOtpScope() {
        return new InvalidLeadOtpContactException("Invalid OTP scope for lead contact tracking");
    }

    public static LeadOtpOperationException saveTrackingFailed(Throwable cause) {
        return new LeadOtpOperationException("Failed to save lead one time token", cause);
    }

    public static LeadOtpOperationException updateTrackingFailed(Throwable cause) {
        return new LeadOtpOperationException("Failed to update lead one time token", cause);
    }

    public static LeadOtpOperationException retrieveTrackingFailed(Throwable cause) {
        return new LeadOtpOperationException("Failed to retrieve lead one time token", cause);
    }
}
