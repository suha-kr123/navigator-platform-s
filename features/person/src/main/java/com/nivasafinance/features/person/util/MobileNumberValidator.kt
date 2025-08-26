package com.nivasafinance.features.person.util

import java.util.regex.Pattern

object MobileNumberValidator {

    // Indian mobile number pattern: 10 digits starting with 6, 7, 8, or 9
    private val INDIAN_MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$")

    /**
     * Validates if the given mobile number is a valid Indian mobile number.
     *
     * @param mobileNumber The mobile number to validate
     * @return true if valid Indian mobile number, false otherwise
     */
    fun isValidIndianMobileNumber(mobileNumber: String?): Boolean {
        if (mobileNumber.isNullOrBlank()) {
            return false
        }

        // Remove any spaces, dashes, or other separators
        val cleanNumber = mobileNumber.replace(Regex("[\\s\\-\\(\\)]"), "")

        return INDIAN_MOBILE_PATTERN.matcher(cleanNumber).matches()
    }

    /**
     * Cleans and formats the mobile number by removing spaces, dashes, and parentheses.
     *
     * @param mobileNumber The mobile number to clean
     * @return Cleaned mobile number or null if input is null/blank
     */
    fun cleanMobileNumber(mobileNumber: String?): String? {
        if (mobileNumber.isNullOrBlank()) {
            return null
        }

        return mobileNumber.replace(Regex("[\\s\\-\\(\\)]"), "")
    }
}
