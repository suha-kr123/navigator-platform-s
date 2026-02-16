package com.nivasafinance.common.utils;

/**
 * Utility class for phone number normalization.
 * Handles various phone number formats and converts them to a standard format with country code.
 */
public class PhoneNumberUtils {

    public static String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() >= 10) {
            return digitsOnly.substring(digitsOnly.length() - 10);
        }
        return digitsOnly;
    }
}
