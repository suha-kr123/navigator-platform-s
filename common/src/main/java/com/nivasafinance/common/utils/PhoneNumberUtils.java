package com.nivasafinance.common.utils;

/**
 * Utility class for phone number normalization.
 * Handles various phone number formats and converts them to a standard format with country code.
 */
public class PhoneNumberUtils {

    /**
     * Normalizes phone number to standard format with country code.
     * Handles formats like: 09982482547, 9982482547, 919982482547, +919982482547
     * Returns: +919982482547
     * 
     * @param phone Phone number in any format
     * @return Normalized phone number with country code prefix (+91 for India)
     */
    public static String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        
        // Remove all non-digit characters except +
        String cleaned = phone.trim();
        
        // If already starts with +, extract digits
        boolean hasPlus = cleaned.startsWith("+");
        String digitsOnly = cleaned.replaceAll("[^0-9]", "");
        
        // If it starts with 0 and is 11 digits, replace 0 with +91 (India country code)
        if (digitsOnly.startsWith("0") && digitsOnly.length() == 11) {
            return "+91" + digitsOnly.substring(1);
        }
        
        // If it's 10 digits, assume India and add +91
        if (digitsOnly.length() == 10) {
            return "+91" + digitsOnly;
        }
        
        // If it's 12 digits and starts with 91, add +
        if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            return "+" + digitsOnly;
        }
        
        // If original already has +, return as is (assuming it's already normalized)
        if (hasPlus) {
            return cleaned;
        }
        
        // Default: return digits only (or you might want to add country code based on length)
        // For now, if it's 10+ digits, assume it needs country code
        if (digitsOnly.length() >= 10) {
            // If it doesn't start with country code, assume India
            if (!digitsOnly.startsWith("91") && digitsOnly.length() == 10) {
                return "+91" + digitsOnly;
            }
            // If it starts with 91 but no +, add +
            if (digitsOnly.startsWith("91") && digitsOnly.length() == 12) {
                return "+" + digitsOnly;
            }
        }
        
        return cleaned;
    }
}

